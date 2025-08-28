  private static void sniff(final HBaseAdmin admin, final Sink sink, HTableDescriptor tableDesc)
      throws Exception {
    HTable table = null;

    try {
      table = new HTable(admin.getConfiguration(), tableDesc.getName());
    } catch (TableNotFoundException e) {
      return;
    }

    for (HRegionInfo region : admin.getTableRegions(tableDesc.getName())) {
      try {
        sniffRegion(admin, sink, region, table);
      } catch (Exception e) {
        sink.publishReadFailure(region, e);
        LOG.debug("sniffRegion failed", e);
      }
    }
  }
