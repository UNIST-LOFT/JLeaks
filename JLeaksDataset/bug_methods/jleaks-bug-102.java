  public int run(String[] args) throws Exception {
    Replication replication;
    ReplicationSourceManager manager;
    FileSystem fs;
    Path oldLogDir, logDir, walRootDir;
    ZKWatcher zkw;

    Abortable abortable = new Abortable() {
      @Override
      public void abort(String why, Throwable e) {
      }

      @Override
      public boolean isAborted() {
        return false;
      }
    };

    zkw =
        new ZKWatcher(conf, "syncupReplication" + System.currentTimeMillis(), abortable,
            true);

    walRootDir = FSUtils.getWALRootDir(conf);
    fs = FSUtils.getWALFileSystem(conf);
    oldLogDir = new Path(walRootDir, HConstants.HREGION_OLDLOGDIR_NAME);
    logDir = new Path(walRootDir, HConstants.HREGION_LOGDIR_NAME);

    System.out.println("Start Replication Server start");
    replication = new Replication();
    replication.initialize(new DummyServer(zkw), fs, logDir, oldLogDir, null);
    manager = replication.getReplicationManager();
    manager.init().get();

    try {
      while (manager.activeFailoverTaskCount() > 0) {
        Thread.sleep(SLEEP_TIME);
      }
      while (manager.getOldSources().size() > 0) {
        Thread.sleep(SLEEP_TIME);
      }
    } catch (InterruptedException e) {
      System.err.println("didn't wait long enough:" + e);
      return (-1);
    }

    manager.join();
    zkw.close();

    return 0;
  }
