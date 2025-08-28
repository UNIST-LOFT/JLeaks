private void unassignMetaReplica(HbckInfo hi) throws IOException, InterruptedException,
KeeperException {
    undeployRegions(hi);
    ZooKeeperWatcher zkw = createZooKeeperWatcher();
    try {
        ZKUtil.deleteNode(zkw, zkw.getZNodeForReplica(hi.metaEntry.getReplicaId()));
    } finally {
        zkw.close();
    }
}