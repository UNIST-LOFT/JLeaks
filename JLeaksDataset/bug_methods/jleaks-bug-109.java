  public Result next() throws IOException {
    Result result = null;
    while (true) {
      if (currentRegionScanner == null) {
        currentRegion++;
        if (currentRegion >= regions.size()) {
          return null;
        }

        HRegionInfo hri = regions.get(currentRegion);
        currentRegionScanner = new ClientSideRegionScanner(conf, fs,
          restoreDir, htd, hri, scan, scanMetrics);
        if (this.scanMetrics != null) {
          this.scanMetrics.countOfRegions.incrementAndGet();
        }
      }

      result = currentRegionScanner.next();
      if (result != null) {
        return result;
      } else {
        currentRegionScanner.close();
        currentRegionScanner = null;
      }
    }
  }
