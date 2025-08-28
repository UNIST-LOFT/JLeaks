    RegionScannerImpl(Scan scan, List<KeyValueScanner> additionalScanners, HRegion region)
        throws IOException {
      this.region = region;
      this.maxResultSize = scan.getMaxResultSize();
      if (scan.hasFilter()) {
        this.filter = new FilterWrapper(scan.getFilter());
      } else {
        this.filter = null;
      }
      this.comparator = region.getCellCompartor();
      /**
       * By default, calls to next/nextRaw must enforce the batch limit. Thus, construct a default
       * scanner context that can be used to enforce the batch limit in the event that a
       * ScannerContext is not specified during an invocation of next/nextRaw
       */
      defaultScannerContext = ScannerContext.newBuilder()
          .setBatchLimit(scan.getBatch()).build();

      if (Bytes.equals(scan.getStopRow(), HConstants.EMPTY_END_ROW) && !scan.isGetScan()) {
        this.stopRow = null;
      } else {
        this.stopRow = scan.getStopRow();
      }
      // If we are doing a get, we want to be [startRow,endRow]. Normally
      // it is [startRow,endRow) and if startRow=endRow we get nothing.
      this.isScan = scan.isGetScan() ? 1 : 0;

      // synchronize on scannerReadPoints so that nobody calculates
      // getSmallestReadPoint, before scannerReadPoints is updated.
      IsolationLevel isolationLevel = scan.getIsolationLevel();
      synchronized(scannerReadPoints) {
        this.readPt = getReadPoint(isolationLevel);
        scannerReadPoints.put(this, this.readPt);
      }

      // Here we separate all scanners into two lists - scanner that provide data required
      // by the filter to operate (scanners list) and all others (joinedScanners list).
      List<KeyValueScanner> scanners = new ArrayList<KeyValueScanner>(scan.getFamilyMap().size());
      List<KeyValueScanner> joinedScanners
        = new ArrayList<KeyValueScanner>(scan.getFamilyMap().size());
      if (additionalScanners != null) {
        scanners.addAll(additionalScanners);
      }

      for (Map.Entry<byte[], NavigableSet<byte[]>> entry : scan.getFamilyMap().entrySet()) {
        Store store = stores.get(entry.getKey());
        KeyValueScanner scanner;
        try {
          scanner = store.getScanner(scan, entry.getValue(), this.readPt);
        } catch (FileNotFoundException e) {
          throw handleFileNotFound(e);
        }
        if (this.filter == null || !scan.doLoadColumnFamiliesOnDemand()
          || this.filter.isFamilyEssential(entry.getKey())) {
          scanners.add(scanner);
        } else {
          joinedScanners.add(scanner);
        }
      }
      initializeKVHeap(scanners, joinedScanners, region);
    }
