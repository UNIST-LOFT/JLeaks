  private long replayRecoveredEdits(final Path edits,
      final long minSeqId, final CancelableProgressable reporter)
    throws IOException {
    String msg = "Replaying edits from " + edits + "; minSequenceid=" +
      minSeqId + "; path=" + edits;
    LOG.info(msg);
    MonitoredTask status = TaskMonitor.get().createStatus(msg);

    status.setStatus("Opening logs");
    HLog.Reader reader = HLog.getReader(this.fs, edits, conf);
    try {
      long currentEditSeqId = minSeqId;
      long firstSeqIdInLog = -1;
      long skippedEdits = 0;
      long editsCount = 0;
      long intervalEdits = 0;
      HLog.Entry entry;
      Store store = null;
      boolean reported_once = false;

      try {
        // How many edits seen before we check elapsed time
        int interval = this.conf.getInt("hbase.hstore.report.interval.edits",
            2000);
        // How often to send a progress report (default 1/2 master timeout)
        int period = this.conf.getInt("hbase.hstore.report.period",
            this.conf.getInt("hbase.master.assignment.timeoutmonitor.timeout",
                180000) / 2);
        long lastReport = EnvironmentEdgeManager.currentTimeMillis();

        while ((entry = reader.next()) != null) {
          HLogKey key = entry.getKey();
          WALEdit val = entry.getEdit();

          if (reporter != null) {
            intervalEdits += val.size();
            if (intervalEdits >= interval) {
              // Number of edits interval reached
              intervalEdits = 0;
              long cur = EnvironmentEdgeManager.currentTimeMillis();
              if (lastReport + period <= cur) {
                status.setStatus("Replaying edits..." +
                    " skipped=" + skippedEdits +
                    " edits=" + editsCount);
                // Timeout reached
                if(!reporter.progress()) {
                  msg = "Progressable reporter failed, stopping replay";
                  LOG.warn(msg);
                  status.abort(msg);
                  throw new IOException(msg);
                }
                reported_once = true;
                lastReport = cur;
              }
            }
          }

          // Start coprocessor replay here. The coprocessor is for each WALEdit
          // instead of a KeyValue.
          if (coprocessorHost != null) {
            status.setStatus("Running pre-WAL-restore hook in coprocessors");
            if (coprocessorHost.preWALRestore(this.getRegionInfo(), key, val)) {
              // if bypass this log entry, ignore it ...
              continue;
            }
          }

          if (firstSeqIdInLog == -1) {
            firstSeqIdInLog = key.getLogSeqNum();
          }
          // Now, figure if we should skip this edit.
          if (key.getLogSeqNum() <= currentEditSeqId) {
            skippedEdits++;
            continue;
          }
          currentEditSeqId = key.getLogSeqNum();
          boolean flush = false;
          for (KeyValue kv: val.getKeyValues()) {
            // Check this edit is for me. Also, guard against writing the special
            // METACOLUMN info such as HBASE::CACHEFLUSH entries
            if (kv.matchingFamily(HLog.METAFAMILY) ||
                !Bytes.equals(key.getEncodedRegionName(), this.regionInfo.getEncodedNameAsBytes())) {
              skippedEdits++;
              continue;
                }
            // Figure which store the edit is meant for.
            if (store == null || !kv.matchingFamily(store.getFamily().getName())) {
              store = this.stores.get(kv.getFamily());
            }
            if (store == null) {
              // This should never happen.  Perhaps schema was changed between
              // crash and redeploy?
              LOG.warn("No family for " + kv);
              skippedEdits++;
              continue;
            }
            // Once we are over the limit, restoreEdit will keep returning true to
            // flush -- but don't flush until we've played all the kvs that make up
            // the WALEdit.
            flush = restoreEdit(store, kv);
            editsCount++;
          }
          if (flush) internalFlushcache(null, currentEditSeqId, status);

          if (coprocessorHost != null) {
            coprocessorHost.postWALRestore(this.getRegionInfo(), key, val);
          }
        }
      } catch (EOFException eof) {
        Path p = HLog.moveAsideBadEditsFile(fs, edits);
        msg = "Encountered EOF. Most likely due to Master failure during " +
            "log spliting, so we have this data in another edit.  " +
            "Continuing, but renaming " + edits + " as " + p;
        LOG.warn(msg, eof);
        status.abort(msg);
      } catch (IOException ioe) {
        // If the IOE resulted from bad file format,
        // then this problem is idempotent and retrying won't help
        if (ioe.getCause() instanceof ParseException) {
          Path p = HLog.moveAsideBadEditsFile(fs, edits);
          msg = "File corruption encountered!  " +
              "Continuing, but renaming " + edits + " as " + p;
          LOG.warn(msg, ioe);
          status.setStatus(msg);
        } else {
          status.abort(StringUtils.stringifyException(ioe));
          // other IO errors may be transient (bad network connection,
          // checksum exception on one datanode, etc).  throw & retry
          throw ioe;
        }
      }
      if (reporter != null && !reported_once) {
        reporter.progress();
      }
      msg = "Applied " + editsCount + ", skipped " + skippedEdits +
        ", firstSequenceidInLog=" + firstSeqIdInLog +
        ", maxSequenceidInLog=" + currentEditSeqId + ", path=" + edits;
      status.markComplete(msg);
      LOG.debug(msg);
      return currentEditSeqId;
    } finally {
      reader.close();
      status.cleanup();
    }
  }
