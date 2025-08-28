  private void unassignMetaReplica(HbckInfo hi) throws IOException, InterruptedException,
  KeeperException {
    undeployRegions(hi);
    ZooKeeperWatcher zkw = createZooKeeperWatcher();
    ZKUtil.deleteNode(zkw, zkw.getZNodeForReplica(hi.metaEntry.getReplicaId()));
  }
