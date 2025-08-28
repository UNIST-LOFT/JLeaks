public boolean reOpenAllRegions(List<HRegionInfo> regions) throws IOException 
{
    boolean done = false;
    LOG.info("Bucketing regions by region server...");
    HTable table = new HTable(masterServices.getConfiguration(), tableName);
    TreeMap<ServerName, List<HRegionInfo>> serverToRegions = Maps.newTreeMap();
    NavigableMap<HRegionInfo, ServerName> hriHserverMapping;
    try {
        hriHserverMapping = table.getRegionLocations();
    } finally {
        table.close();
    }
    List<HRegionInfo> reRegions = new ArrayList<HRegionInfo>();
    for (HRegionInfo hri : regions) {
        ServerName rsLocation = hriHserverMapping.get(hri);
        // Skip the offlined split parent region
        // See HBASE-4578 for more information.
        if (null == rsLocation) {
            LOG.info("Skip " + hri);
            continue;
        }
        if (!serverToRegions.containsKey(rsLocation)) {
            LinkedList<HRegionInfo> hriList = Lists.newLinkedList();
            serverToRegions.put(rsLocation, hriList);
        }
        reRegions.add(hri);
        serverToRegions.get(rsLocation).add(hri);
    }
    LOG.info("Reopening " + reRegions.size() + " regions on " + serverToRegions.size() + " region servers.");
    this.masterServices.getAssignmentManager().setRegionsToReopen(reRegions);
    BulkReOpen bulkReopen = new BulkReOpen(this.server, serverToRegions, this.masterServices.getAssignmentManager());
    while (true) {
        try {
            if (bulkReopen.bulkReOpen()) {
                done = true;
                break;
            } else {
                LOG.warn("Timeout before reopening all regions");
            }
        } catch (InterruptedException e) {
            LOG.warn("Reopen was interrupted");
            // Preserve the interrupt.
            Thread.currentThread().interrupt();
            break;
        }
    }
    return done;
}