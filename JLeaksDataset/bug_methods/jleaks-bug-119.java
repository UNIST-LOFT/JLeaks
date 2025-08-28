  private List<Path> splitLog(final FileStatus[] logfiles) throws IOException {
    List<Path> processedLogs = new ArrayList<Path>();
    List<Path> corruptedLogs = new ArrayList<Path>();
    List<Path> splits = null;

    boolean skipErrors = conf.getBoolean("hbase.hlog.split.skip.errors", true);

    countTotalBytes(logfiles);
    splitSize = 0;

    outputSink.startWriterThreads(entryBuffers);

    try {
      int i = 0;
      for (FileStatus log : logfiles) {
       Path logPath = log.getPath();
        long logLength = log.getLen();
        splitSize += logLength;
        logAndReport("Splitting hlog " + (i++ + 1) + " of " + logfiles.length
            + ": " + logPath + ", length=" + logLength);
        Reader in;
        try {
          in = getReader(fs, log, conf, skipErrors);
          if (in != null) {
            parseHLog(in, logPath, entryBuffers, fs, conf, skipErrors);
            try {
              in.close();
            } catch (IOException e) {
              LOG.warn("Close log reader threw exception -- continuing",
                  e);
            }
          }
          processedLogs.add(logPath);
        } catch (CorruptedLogFileException e) {
          LOG.info("Got while parsing hlog " + logPath +
              ". Marking as corrupted", e);
          corruptedLogs.add(logPath);
          continue;
        }
      }
      status.setStatus("Log splits complete. Checking for orphaned logs.");
      
      if (fs.listStatus(srcDir).length > processedLogs.size()
          + corruptedLogs.size()) {
        throw new OrphanHLogAfterSplitException(
            "Discovered orphan hlog after split. Maybe the "
            + "HRegionServer was not dead when we started");
      }
    } finally {
      status.setStatus("Finishing writing output logs and closing down.");
      splits = outputSink.finishWritingAndClose();
    }
    status.setStatus("Archiving logs after completed split");
    archiveLogs(srcDir, corruptedLogs, processedLogs, oldLogDir, fs, conf);
    return splits;
  }
