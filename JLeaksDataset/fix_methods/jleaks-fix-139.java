static void createPresplitTable(String tableName, SplitAlgorithm splitAlgo,
String[] columnFamilies, Configuration conf) throws IOException,
InterruptedException {
    final int splitCount = conf.getInt("split.count", 0);
    Preconditions.checkArgument(splitCount > 1, "Split count must be > 1");
    Preconditions.checkArgument(columnFamilies.length > 0, "Must specify at least one column family. ");
    LOG.debug("Creating table " + tableName + " with " + columnFamilies.length + " column families.  Presplitting to " + splitCount + " regions");
    HTableDescriptor desc = new HTableDescriptor(tableName);
    for (String cf : columnFamilies) {
        desc.addFamily(new HColumnDescriptor(Bytes.toBytes(cf)));
    }
    HBaseAdmin admin = new HBaseAdmin(conf);
    Preconditions.checkArgument(!admin.tableExists(tableName), "Table already exists: " + tableName);
    admin.createTable(desc, splitAlgo.split(splitCount));
    LOG.debug("Table created!  Waiting for regions to show online in META...");
    if (!conf.getBoolean("split.verify", true)) {
        // NOTE: createTable is synchronous on the table, but not on the regions
        HTable table = new HTable(conf, tableName);
        int onlineRegions = 0;
        while (onlineRegions < splitCount) {
            onlineRegions = table.getRegionsInfo().size();
            LOG.debug(onlineRegions + " of " + splitCount + " regions online...");
            if (onlineRegions < splitCount) {
                // sleep
                Thread.sleep(10 * 1000);
            }
        }
        table.close();
    }
    LOG.debug("Finished creating table with " + splitCount + " regions");
}