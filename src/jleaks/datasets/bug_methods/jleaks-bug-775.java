    public boolean doesColumnExist(String tableName, String columnName) {
        ResultSet resultSet = null;
        boolean result;

        try {
        	Connection connection;
        	
            LOG.finest("Checking if column {" + columnName + "} in table {" + tableName + "} exists.");

            // This connection may not be freed if an exception occurs. It's a small chance and the
			// additional code to avoid it is cumbersome.
            connection = DataSourceUtils.getConnection(dataSource);
            
            resultSet = connection.getMetaData().getColumns(null, null, tableName, columnName);
            result = resultSet.next();
            resultSet.close();
            resultSet = null;
            
            DataSourceUtils.releaseConnection(connection, dataSource);

            return result;

        } catch (SQLException e) {
            throw new OsmosisRuntimeException("Unable to check for the existence of column " + tableName + "."
                    + columnName + ".", e);
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    // We are already in an error condition so log and continue.
                    LOG.log(Level.WARNING, "Unable to close column existence result set.", e);
                }
            }
        }
    }
