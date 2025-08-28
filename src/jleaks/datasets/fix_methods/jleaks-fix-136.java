ConnectionImplementation(Configuration conf,
ExecutorService pool, User user) throws IOException {
    this.conf = conf;
    this.user = user;
    this.batchPool = pool;
    this.tableConfig = new TableConfiguration(conf);
    this.closed = false;
    this.pause = conf.getLong(HConstants.HBASE_CLIENT_PAUSE,
        HConstants.DEFAULT_HBASE_CLIENT_PAUSE);
    this.useMetaReplicas = conf.getBoolean(HConstants.USE_META_REPLICAS,
      HConstants.DEFAULT_USE_META_REPLICAS);
    this.numTries = tableConfig.getRetriesNumber();
    this.rpcTimeout = conf.getInt(
      HConstants.HBASE_RPC_TIMEOUT_KEY,
      HConstants.DEFAULT_HBASE_RPC_TIMEOUT);
    if (conf.getBoolean(CLIENT_NONCES_ENABLED_KEY, true)) {
      synchronized (nonceGeneratorCreateLock) {
        if (nonceGenerator == null) {
          nonceGenerator = new PerClientRandomNonceGenerator();
        }
      }
    } else {
      nonceGenerator = new NoNonceGenerator();
    }

    this.stats = ServerStatisticTracker.create(conf);
    this.interceptor = (new RetryingCallerInterceptorFactory(conf)).build();
    this.rpcControllerFactory = RpcControllerFactory.instantiate(conf);
    this.rpcCallerFactory = RpcRetryingCallerFactory.instantiate(conf, interceptor, this.stats);
    this.backoffPolicy = ClientBackoffPolicyFactory.create(conf);
    this.asyncProcess = createAsyncProcess(this.conf);

    boolean shouldListen = conf.getBoolean(HConstants.STATUS_PUBLISHED,
        HConstants.STATUS_PUBLISHED_DEFAULT);
    Class<? extends ClusterStatusListener.Listener> listenerClass =
        conf.getClass(ClusterStatusListener.STATUS_LISTENER_CLASS,
            ClusterStatusListener.DEFAULT_STATUS_LISTENER_CLASS,
            ClusterStatusListener.Listener.class);

    try {
      this.registry = setupRegistry();
      retrieveClusterId();

      this.rpcClient = RpcClientFactory.createClient(this.conf, this.clusterId);

      // Do we publish the status?
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
    } catch (Throwable e) {
      // avoid leaks: registry, rpcClient, ...
      LOG.debug("connection construction failed", e);
      close();
      throw e;
    }
  }