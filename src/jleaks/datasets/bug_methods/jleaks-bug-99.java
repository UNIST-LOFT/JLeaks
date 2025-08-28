  public static void setupRegionReplicaReplication(Configuration conf) throws IOException {
    if (!isRegionReplicaReplicationEnabled(conf)) {
      return;
    }
    Admin admin = ConnectionFactory.createConnection(conf).getAdmin();
    ReplicationPeerConfig peerConfig = null;
    try {
      peerConfig = admin.getReplicationPeerConfig(REGION_REPLICA_REPLICATION_PEER);
    } catch (ReplicationPeerNotFoundException e) {
      LOG.warn("Region replica replication peer id=" + REGION_REPLICA_REPLICATION_PEER
          + " not exist", e);
    }
    try {
      if (peerConfig == null) {
        LOG.info("Region replica replication peer id=" + REGION_REPLICA_REPLICATION_PEER
            + " not exist. Creating...");
        peerConfig = new ReplicationPeerConfig();
        peerConfig.setClusterKey(ZKConfig.getZooKeeperClusterKey(conf));
        peerConfig.setReplicationEndpointImpl(RegionReplicaReplicationEndpoint.class.getName());
        admin.addReplicationPeer(REGION_REPLICA_REPLICATION_PEER, peerConfig);
      }
    } finally {
      admin.close();
    }
  }
