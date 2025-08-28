public ZooKeeperWatcher(Configuration conf, String identifier,
      Abortable abortable, boolean canCreateBaseZNode)
  throws IOException, ZooKeeperConnectionException {
    this.conf = conf;
    this.quorum = ZKConfig.getZKQuorumServersString(conf);
    this.prefix = identifier;
    // Identifier will get the sessionid appended later below down when we
    // handle the syncconnect event.
    this.identifier = identifier + "0x0";
    this.abortable = abortable;
    setNodeNames(conf);
    PendingWatcher pendingWatcher = new PendingWatcher();
    this.recoverableZooKeeper = ZKUtil.connect(conf, quorum, pendingWatcher, identifier);
    pendingWatcher.prepare(this);
    if (canCreateBaseZNode) {
      try {
        createBaseZNodes();
      } catch (ZooKeeperConnectionException zce) {
        try {
          this.recoverableZooKeeper.close();
        } catch (InterruptedException ie) {
          LOG.debug("Encountered InterruptedException when closing " + this.recoverableZooKeeper);
          Thread.currentThread().interrupt();
        }
        throw zce;
      }
    }
  }
