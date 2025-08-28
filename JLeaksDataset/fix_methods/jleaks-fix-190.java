public Result getCurrentRowState(Mutation m, Collection<? extends ColumnReference> columns, boolean ignoreNewerMutations){
    byte[] row = m.getRow();
    // need to use a scan here so we can get raw state, which Get doesn't provide.
    Scan s = IndexManagementUtil.newLocalStateScan(Collections.singletonList(columns));
    s.setStartRow(row);
    s.setStopRow(row);
    if (ignoreNewerMutations) {
        // Provides a means of client indicating that newer cells should not be considered,
        // enabling mutations to be replayed to partially rebuild the index when a write fails.
        // When replaying mutations we want the oldest timestamp (as anything newer we be replayed)
        long ts = getOldestTimestamp(m.getFamilyCellMap().values());
        s.setTimeRange(0, ts);
    }
    Region region = this.env.getRegion();
    try (RegionScanner scanner = region.getScanner(s)) {
        List<Cell> kvs = new ArrayList<Cell>(1);
        boolean more = scanner.next(kvs);
        assert !more : "Got more than one result when scanning" + " a single row in the primary table!";
        Result r = Result.create(kvs);
        return r;
    }
}