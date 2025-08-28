public SolrZkClient(String zkServerAddress, int zkClientTimeout,
ZkClientConnectionStrategy strat, final OnReconnect onReconnect, int clientConnectTimeout) throws InterruptedException,
TimeoutException, IOException {
    connManager = new ConnectionManager("ZooKeeperConnection Watcher:"
        + zkServerAddress, this, zkServerAddress, zkClientTimeout, strat, onReconnect);
    try {
      strat.connect(zkServerAddress, zkClientTimeout, connManager,
          new ZkUpdate() {
            @Override
            public void update(SolrZooKeeper zooKeeper) {
              SolrZooKeeper oldKeeper = keeper;
              keeper = zooKeeper;
              try {
                closeKeeper(oldKeeper);
              } finally {
                if (isClosed) {
                  // we may have been closed
                  closeKeeper(SolrZkClient.this.keeper);
                }
              }
            }
          });
    } catch (IOException e) {
      connManager.close();
      throw new RuntimeException();
    } catch (InterruptedException e) {
      connManager.close();
      throw new RuntimeException();
    } catch (TimeoutException e) {
      connManager.close();
      throw new RuntimeException();
    }
    try {
      connManager.waitForConnected(clientConnectTimeout);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      connManager.close();
      throw new RuntimeException();
    } catch (TimeoutException e) {
      connManager.close();
      throw new RuntimeException();
    }
    numOpens.incrementAndGet();
  }