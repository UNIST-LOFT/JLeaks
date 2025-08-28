public void executeIdTableDropStatements(
    String[] dropStatements,
    JdbcServices jdbcServices,
    JdbcConnectionAccess connectionAccess) {
    if (dropStatements == null) {
        return;
    }
    try {
        Connection connection = connectionAccess.obtainConnection();
        try {
            // TODO: session.getTransactionCoordinator().getJdbcCoordinator().getStatementPreparer().createStatement();
            try (java.sql.Statement statement = connection.createStatement()) {
                for (String dropStatement : dropStatements) {
                    try {
                        jdbcServices.getSqlStatementLogger().logStatement(dropStatement);
                        statement.execute(dropStatement);
                    } catch (java.sql.SQLException e) {
                        log.debugf("Error attempting to cleanup id-table : [%s]", e.getMessage());
                    }
                }
                // TODO
                // session.getTransactionCoordinator().getJdbcCoordinator().release( statement );
            }
        } catch (SQLException e) {
            log.error("Unable to use JDBC Connection to create Statement", e);
        } finally {
            try {
                connectionAccess.releaseConnection(connection);
            } catch (SQLException ignore) {
            }
        }
    } catch (SQLException e) {
        log.error("Unable obtain JDBC Connection", e);
    }
}