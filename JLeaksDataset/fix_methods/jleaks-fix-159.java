private void handleShardCheckpointAction(SolrQueryRequest req, SolrQueryResponse rsp) 
{
    if (!leaderStateManager.amILeader()) {
        throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "Action '" + CdcrParams.CdcrAction.SHARDCHECKPOINT + "' sent to non-leader replica");
    }
    UpdateLog ulog = core.getUpdateHandler().getUpdateLog();
    try (UpdateLog.RecentUpdates recentUpdates = ulog.getRecentUpdates()) {
        List<Long> versions = recentUpdates.getVersions(1);
        long lastVersion = versions.isEmpty() ? -1 : Math.abs(versions.get(0));
        rsp.add(CdcrParams.CHECKPOINT, lastVersion);
    }
}