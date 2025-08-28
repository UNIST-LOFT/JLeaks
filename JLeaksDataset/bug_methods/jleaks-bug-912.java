    private void getTables() throws SQLException {

        
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(
                "SELECT name, sql FROM sqlite_master "
                + " WHERE type= 'table' "
                + " ORDER BY name;"); //NON-NLS

        while (resultSet.next()) {
            String tableName = resultSet.getString("name"); //NON-NLS
            String tableSQL = resultSet.getString("sql"); //NON-NLS

            dbTablesMap.put(tableName, tableSQL);
        }
        resultSet.close();
	statement.close();
    }
