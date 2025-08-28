    public PhoenixRecordWriter(final Configuration configuration, Set<String> propsToIgnore) throws SQLException {
        this.conn = ConnectionUtil.getOutputConnectionWithoutTheseProps(configuration, propsToIgnore);
        this.batchSize = PhoenixConfigurationUtil.getBatchSize(configuration);
        final String upsertQuery = PhoenixConfigurationUtil.getUpsertStatement(configuration);
        this.statement = this.conn.prepareStatement(upsertQuery);
    }
