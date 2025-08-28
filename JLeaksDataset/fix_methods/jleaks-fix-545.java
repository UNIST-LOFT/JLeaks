public RawResults queryForAllRawOld(ConnectionSource connectionSource, String query) throws SQLException 
{
    DatabaseConnection connection = connectionSource.getReadOnlyConnection();
    CompiledStatement compiledStatement = null;
    try {
        compiledStatement = connection.compileStatement(query, StatementType.SELECT, noFieldTypes);
        String[] columnNames = extractColumnNames(compiledStatement);
        RawResultsWrapper rawResults = new RawResultsWrapper(connectionSource, connection, query, compiledStatement, columnNames, this);
        compiledStatement = null;
        connection = null;
        return rawResults;
    } finally {
        if (compiledStatement != null) {
            compiledStatement.close();
        }
        if (connection != null) {
            connectionSource.releaseConnection(connection);
        }
    }
}