  public boolean splitLogFileToTemp(FileStatus logfile, String tmpname,
      CancelableProgressable reporter)
  throws IOException {
    final Map<byte[], Object> logWriters = Collections.
    synchronizedMap(new TreeMap<byte[], Object>(Bytes.BYTES_COMPARATOR));
    boolean isCorrupted = false;
    Preconditions.checkState(status == null);
    status = TaskMonitor.get().createStatus(
        "Splitting log file " + logfile.getPath() +
        "into a temporary staging area.");

    Object BAD_WRITER = new Object();

    boolean progress_failed = false;

    boolean skipErrors = conf.getBoolean("hbase.hlog.split.skip.errors",
        HLog.SPLIT_SKIP_ERRORS_DEFAULT);
    int interval = conf.getInt("hbase.splitlog.report.interval.loglines", 1024);
    // How often to send a progress report (default 1/2 the zookeeper session
    // timeout of if that not set, the split log DEFAULT_TIMEOUT)
    int period = conf.getInt("hbase.splitlog.report.period",
      conf.getInt("hbase.splitlog.manager.timeout", SplitLogManager.DEFAULT_TIMEOUT) / 2);
    int numOpenedFilesBeforeReporting =
      conf.getInt("hbase.splitlog.report.openedfiles", 3);
    Path logPath = logfile.getPath();
    long logLength = logfile.getLen();
    LOG.info("Splitting hlog: " + logPath + ", length=" + logLength);
    status.setStatus("Opening log file");
    Reader in = null;
    try {
      in = getReader(fs, logfile, conf, skipErrors);
    } catch (CorruptedLogFileException e) {
      LOG.warn("Could not get reader, corrupted log file " + logPath, e);
      ZKSplitLog.markCorrupted(rootDir, tmpname, fs);
      isCorrupted = true;
    }
    if (in == null) {
      status.markComplete("Was nothing to split in log file");
      LOG.warn("Nothing to split in log file " + logPath);
      return true;
    }
    long t = EnvironmentEdgeManager.currentTimeMillis();
    long last_report_at = t;
    if (reporter != null && reporter.progress() == false) {
      status.markComplete("Failed: reporter.progress asked us to terminate");
      return false;
    }
    // Report progress every so many edits and/or files opened (opening a file
    // takes a bit of time).
    int editsCount = 0;
    int numNewlyOpenedFiles = 0;
    Entry entry;
    try {
      while ((entry = getNextLogLine(in,logPath, skipErrors)) != null) {
        byte[] region = entry.getKey().getEncodedRegionName();
        Object o = logWriters.get(region);
        if (o == BAD_WRITER) {
          continue;
        }
        WriterAndPath wap = (WriterAndPath)o;
        if (wap == null) {
          wap = createWAP(region, entry, rootDir, tmpname, fs, conf);
          numNewlyOpenedFiles++;
          if (wap == null) {
            // ignore edits from this region. It doesn't exist anymore.
            // It was probably already split.
            logWriters.put(region, BAD_WRITER);
            continue;
          } else {
            logWriters.put(region, wap);
          }
        }
        wap.w.append(entry);
        outputSink.updateRegionMaximumEditLogSeqNum(entry);
        editsCount++;
        // If sufficient edits have passed OR we've opened a few files, check if
        // we should report progress.
        if (editsCount % interval == 0 ||
            (numNewlyOpenedFiles > numOpenedFilesBeforeReporting)) {
          // Zero out files counter each time we fall in here.
          numNewlyOpenedFiles = 0;
          String countsStr = "edits=" + editsCount + ", files=" + logWriters.size();
          status.setStatus("Split " + countsStr);
          long t1 = EnvironmentEdgeManager.currentTimeMillis();
          if ((t1 - last_report_at) > period) {
            last_report_at = t;
            if (reporter != null && reporter.progress() == false) {
              status.markComplete("Failed: reporter.progress asked us to terminate; " + countsStr);
              progress_failed = true;
              return false;
            }
          }
        }
      }
    } catch (CorruptedLogFileException e) {
      LOG.warn("Could not parse, corrupted log file " + logPath, e);
      ZKSplitLog.markCorrupted(rootDir, tmpname, fs);
      isCorrupted = true;
    } catch (IOException e) {
      e = RemoteExceptionHandler.checkIOException(e);
      throw e;
    } finally {
      int n = 0;
      for (Map.Entry<byte[], Object> logWritersEntry : logWriters.entrySet()) {
        Object o = logWritersEntry.getValue();
        long t1 = EnvironmentEdgeManager.currentTimeMillis();
        if ((t1 - last_report_at) > period) {
          last_report_at = t;
          if ((progress_failed == false) && (reporter != null) &&
              (reporter.progress() == false)) {
            progress_failed = true;
          }
        }
        if (o == BAD_WRITER) {
          continue;
        }
        n++;
        WriterAndPath wap = (WriterAndPath)o;
        wap.w.close();
        LOG.debug("Closed " + wap.p);
        Path dst = getCompletedRecoveredEditsFilePath(wap.p,
          outputSink.getRegionMaximumEditLogSeqNum(logWritersEntry.getKey()));
        if (!dst.equals(wap.p) && fs.exists(dst)) {
          LOG.warn("Found existing old edits file. It could be the "
              + "result of a previous failed split attempt. Deleting " + dst
              + ", length=" + fs.getFileStatus(dst).getLen());
          if (!fs.delete(dst, false)) {
            LOG.warn("Failed deleting of old " + dst);
            throw new IOException("Failed deleting of old " + dst);
          }
        }
        // Skip the unit tests which create a splitter that reads and writes the
        // data without touching disk. TestHLogSplit#testThreading is an
        // example.
        if (fs.exists(wap.p)) {
          if (!fs.rename(wap.p, dst)) {
            throw new IOException("Failed renaming " + wap.p + " to " + dst);
          }
          LOG.debug("Rename " + wap.p + " to " + dst);
        }
      }
      String msg = "Processed " + editsCount + " edits across " + n + " regions" +
        " threw away edits for " + (logWriters.size() - n) + " regions" +
        "; log file=" + logPath + " is corrupted = " + isCorrupted +
        " progress failed = " + progress_failed;
      LOG.info(msg);
      status.markComplete(msg);
    }
    return !progress_failed;
  }
