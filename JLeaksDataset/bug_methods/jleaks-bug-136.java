  ConnectionImplementation(Configuration conf,
                           ExecutorService pool, User user) throws IOException {
    this(conf);
    this.user = user;
    this.batchPool = pool;
    this.registry = setupRegistry();
    retrieveClusterId();

    this.rpcClient = RpcClientFactory.createClient(this.conf, this.clusterId);
    this.rpcControllerFactory = RpcControllerFactory.instantiate(conf);

    // Do we publish the status?
    boolean shouldListen = conf.getBoolean(HConstants.STATUS_PUBLISHED,
        HConstants.STATUS_PUBLISHED_DEFAULT);
    Class<? extends ClusterStatusListener.Listener> listenerClass =
        conf.getClass(ClusterStatusListener.STATUS_LISTENER_CLASS,
            ClusterStatusListener.DEFAULT_STATUS_LISTENER_CLASS,
            ClusterStatusListener.Listener.class);
    if (shouldListen) {
      if (listenerClass == null) {
        LOG.warn(HConstants.STATUS_PUBLISHED + " is true, but " +
            ClusterStatusListener.STATUS_LISTENER_CLASS + " is not set - not listening status");
      } else {
        clusterStatusListener = new ClusterStatusListener(
            new ClusterStatusListener.DeadServerHandler() {
              @Override
              public void newDead(ServerName sn) {
                clearCaches(sn);
                rpcClient.cancelConnections(sn);
              }
            }, conf, listenerClass);
      }
    }
  }
