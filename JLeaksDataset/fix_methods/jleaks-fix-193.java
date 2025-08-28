
    public PhoenixRecordWriter(final Configuration configuration, Set<String> propsToIgnore) throws SQLException {
        Connection connection = null;
        try {
            connection = ConnectionUtil.getOutputConnectionWithoutTheseProps(configuration, propsToIgnore);
            this.batchSize = PhoenixConfigurationUtil.getBatchSize(configuration);
            final String upsertQuery = PhoenixConfigurationUtil.getUpsertStatement(configuration);
            this.statement = connection.prepareStatement(upsertQuery);
            this.conn = connection;
        } catch (Exception e) {
            // Only close the connection in case of an exception, so cannot use try-with-resources
            if (connection != null) {
                connection.close();
            }
            throw e;
        }
    }