public boolean doesColumnExist(String tableName, String columnName)
{
    LOG.finest("Checking if column {" + columnName + "} in table {" + tableName + "} exists.");
    Connection connection = DataSourceUtils.getConnection(dataSource);
    try (ResultSet resultSet = connection.getMetaData().getColumns(null, null, tableName, columnName)) {
        return resultSet.next();
    } catch (SQLException e) {
        throw new OsmosisRuntimeException("Unable to check for the existence of column " + tableName + "." + columnName + ".", e);
    } finally {
        DataSourceUtils.releaseConnection(connection, dataSource);
    }
}