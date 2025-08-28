  public static void metaScan(Configuration configuration,
      final MetaScannerVisitor visitor, final byte[] tableName,
      final byte[] row, final int rowLimit, final byte[] metaTableName)
  throws IOException {
    int rowUpperLimit = rowLimit > 0 ? rowLimit: Integer.MAX_VALUE;
    HTable metaTable = new HTable(configuration, HConstants.META_TABLE_NAME);
    // Calculate startrow for scan.
    byte[] startRow;
    try {
      if (row != null) {
        // Scan starting at a particular row in a particular table
        byte[] searchRow = HRegionInfo.createRegionName(tableName, row, HConstants.NINES, false);
        Result startRowResult = metaTable.getRowOrBefore(searchRow, HConstants.CATALOG_FAMILY);
        if (startRowResult == null) {
          throw new TableNotFoundException("Cannot find row in .META. for table: " +
            Bytes.toString(tableName) + ", row=" + Bytes.toStringBinary(searchRow));
        }
        HRegionInfo regionInfo = getHRegionInfo(startRowResult);
        if (regionInfo == null) {
          throw new IOException("HRegionInfo was null or empty in Meta for " +
            Bytes.toString(tableName) + ", row=" + Bytes.toStringBinary(searchRow));
        }
        byte[] rowBefore = regionInfo.getStartKey();
        startRow = HRegionInfo.createRegionName(tableName, rowBefore, HConstants.ZEROES, false);
      } else if (tableName == null || tableName.length == 0) {
        // Full META scan
        startRow = HConstants.EMPTY_START_ROW;
      } else {
        // Scan META for an entire table
        startRow = HRegionInfo.createRegionName(tableName, HConstants.EMPTY_START_ROW,
          HConstants.ZEROES, false);
      }
      final Scan scan = new Scan(startRow).addFamily(HConstants.CATALOG_FAMILY);
      int rows = Math.min(rowLimit, configuration.getInt(HConstants.HBASE_META_SCANNER_CACHING,
        HConstants.DEFAULT_HBASE_META_SCANNER_CACHING));
      scan.setCaching(rows);
      if (LOG.isDebugEnabled()) {
        LOG.debug("Scanning " + Bytes.toString(metaTableName) + " starting at row=" +
          Bytes.toStringBinary(startRow) + " for max=" + rowUpperLimit + " with caching=" + rows);
      }
      // Run the scan
      ResultScanner scanner = metaTable.getScanner(scan);
      Result result = null;
      int processedRows = 0;
      while ((result = scanner.next()) != null) {
        if (visitor != null) {
          if (!visitor.processRow(result)) break;
        }
        processedRows++;
        if (processedRows >= rowUpperLimit) break;
      }
    } finally {
      if (visitor != null) visitor.close();
      if (metaTable != null) metaTable.close();
    }
  }
