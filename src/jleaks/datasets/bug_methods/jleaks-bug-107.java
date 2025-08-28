  public List<Path> flushSnapshot(MemStoreSnapshot snapshot, long cacheFlushId,
      MonitoredTask status) throws IOException {
    ArrayList<Path> result = new ArrayList<Path>();
    int cellsCount = snapshot.getCellsCount();
    if (cellsCount == 0) return result; // don't flush if there are no entries

    // Use a store scanner to find which rows to flush.
    long smallestReadPoint = store.getSmallestReadPoint();
    InternalScanner scanner = createScanner(snapshot.getScanner(), smallestReadPoint);
    if (scanner == null) {
      return result; // NULL scanner returned from coprocessor hooks means skip normal processing
    }

    StoreFile.Writer writer;
    try {
      // TODO:  We can fail in the below block before we complete adding this flush to
      //        list of store files.  Add cleanup of anything put on filesystem if we fail.
      synchronized (flushLock) {
        status.setStatus("Flushing " + store + ": creating writer");
        // Write the map out to the disk
        writer = store.createWriterInTmp(
            cellsCount, store.getFamily().getCompression(), false, true, true);
        writer.setTimeRangeTracker(snapshot.getTimeRangeTracker());
        try {
          performFlush(scanner, writer, smallestReadPoint);
        } finally {
          finalizeWriter(writer, cacheFlushId, status);
        }
      }
    } finally {
      scanner.close();
    }
    LOG.info("Flushed, sequenceid=" + cacheFlushId +", memsize="
        + StringUtils.humanReadableInt(snapshot.getSize()) +
        ", hasBloomFilter=" + writer.hasGeneralBloom() +
        ", into tmp file " + writer.getPath());
    result.add(writer.getPath());
    return result;
  }
