private void getTables() throws SQLException 
{
    Statement statement = null;
    ResultSet resultSet = null;
    try {
        statement = connection.createStatement();
        resultSet = statement.executeQuery("SELECT name, sql FROM sqlite_master " + " WHERE type= 'table' " + // NON-NLS
        " ORDER BY name;");
        while (resultSet.next()) {
            // NON-NLS
            String tableName = resultSet.getString("name");
            // NON-NLS
            String tableSQL = resultSet.getString("sql");
            dbTablesMap.put(tableName, tableSQL);
        }
    } catch (SQLException ex) {
        throw ex;
    } finally {
        if (null != resultSet) {
            resultSet.close();
        }
        if (null != statement) {
            statement.close();
        }
    }
}