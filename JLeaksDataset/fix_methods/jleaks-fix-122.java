public List<KeyValueScanner> getScanners(boolean cacheBlocks, boolean usePread,
      boolean isCompaction, ScanQueryMatcher matcher, byte[] startRow, boolean includeStartRow,
      byte[] stopRow, boolean includeStopRow, long readPt) throws IOException {
    Collection<HStoreFile> storeFilesToScan;
    List<KeyValueScanner> memStoreScanners;
    this.lock.readLock().lock();
    try {
      storeFilesToScan = this.storeEngine.getStoreFileManager().getFilesForScan(startRow,
        includeStartRow, stopRow, includeStopRow);
      memStoreScanners = this.memstore.getScanners(readPt);
    } finally {
      this.lock.readLock().unlock();
    }

    try {
      // First the store file scanners

      // TODO this used to get the store files in descending order,
      // but now we get them in ascending order, which I think is
      // actually more correct, since memstore get put at the end.
      List<StoreFileScanner> sfScanners = StoreFileScanner
        .getScannersForStoreFiles(storeFilesToScan, cacheBlocks, usePread, isCompaction, false,
          matcher, readPt);
      List<KeyValueScanner> scanners = new ArrayList<>(sfScanners.size() + 1);
      scanners.addAll(sfScanners);
      // Then the memstore scanners
      scanners.addAll(memStoreScanners);
      return scanners;
    } catch (Throwable t) {
      clearAndClose(memStoreScanners);
      throw t instanceof IOException ? (IOException) t : new IOException(t);
    }
  }