public RecoveryIndexWriter(IndexFailurePolicy policy, RegionCoprocessorEnvironment env, String name)
            throws IOException {
        super(new TrackingParallelWriterIndexCommitter(), policy, env, name);
        Connection hConn = null;
        try {
            hConn = ConnectionFactory.createConnection(env.getConfiguration());
            this.admin = hConn.getAdmin();
        } catch (Exception e) {
            // Close the connection only if an exception occurs
            if (hConn != null) {
                hConn.close();
            }
            throw e;
        }
    }