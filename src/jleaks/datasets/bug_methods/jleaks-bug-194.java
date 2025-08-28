    public RecoveryIndexWriter(IndexFailurePolicy policy, RegionCoprocessorEnvironment env, String name)
            throws MasterNotRunningException, ZooKeeperConnectionException, IOException {
        super(new TrackingParallelWriterIndexCommitter(), policy, env, name);
        this.admin = ConnectionFactory.createConnection(env.getConfiguration()).getAdmin();
    }
