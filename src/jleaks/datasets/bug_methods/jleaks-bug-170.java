  public SolrZkClient(String zkServerAddress, int zkClientTimeout,
      ZkClientConnectionStrategy strat, final OnReconnect onReconnect, int clientConnectTimeout) throws InterruptedException,
      TimeoutException, IOException {
    connManager = new ConnectionManager("ZooKeeperConnection Watcher:"
        + zkServerAddress, this, zkServerAddress, zkClientTimeout, strat, onReconnect);
    strat.connect(zkServerAddress, zkClientTimeout, connManager,
        new ZkUpdate() {
          @Override
          public void update(SolrZooKeeper zooKeeper) {
            SolrZooKeeper oldKeeper = keeper;
            keeper = zooKeeper;
            if (oldKeeper != null) {
              try {
                oldKeeper.close();
              } catch (InterruptedException e) {
                // Restore the interrupted status
                Thread.currentThread().interrupt();
                log.error("", e);
                throw new ZooKeeperException(SolrException.ErrorCode.SERVER_ERROR,
                    "", e);
              }
            }
            if (isClosed) {
              // we may have been closed
              try {
                SolrZkClient.this.keeper.close();
              } catch (InterruptedException e) {
                // Restore the interrupted status
                Thread.currentThread().interrupt();
                log.error("", e);
                throw new ZooKeeperException(SolrException.ErrorCode.SERVER_ERROR,
                    "", e);
              }
            }
          }
        });
    connManager.waitForConnected(clientConnectTimeout);
    numOpens.incrementAndGet();
  }
