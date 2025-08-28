private void fetchMetaData() 
{
    Connection connection = null;
    ResultSet rs = null;
    ResultSet tables = null;
    try {
        connection = getConnection();
        DatabaseMetaData dbmd = connection.getMetaData();
        if (dbmd != null) {
            tableName = SQLUtil.escapeSQL(tableName);
            tables = dbmd.getTables(null, null, tableName, null);
            if (!tables.next()) {
                tables = dbmd.getTables(null, null, tableName.toUpperCase(), null);
                if (!tables.next()) {
                    throw new IllegalArgumentException("Table with the name \"" + tableName + "\" was not found. Check your database contents.");
                } else {
                    tableName = tableName.toUpperCase();
                }
            }
            tables.close();
            rs = dbmd.getPrimaryKeys(null, null, tableName);
            List<String> names = new ArrayList<String>();
            while (rs.next()) {
                names.add(rs.getString("COLUMN_NAME"));
            }
            rs.close();
            if (!names.isEmpty()) {
                primaryKeyColumns = names;
            }
            if (primaryKeyColumns == null || primaryKeyColumns.isEmpty()) {
                throw new IllegalArgumentException("Primary key constraints have not been defined for the table \"" + tableName + "\". Use FreeFormQuery to access this table.");
            }
            for (String colName : primaryKeyColumns) {
                if (colName.equalsIgnoreCase("rownum")) {
                    if (getSqlGenerator() instanceof MSSQLGenerator || getSqlGenerator() instanceof MSSQLGenerator) {
                        throw new IllegalArgumentException("When using Oracle or MSSQL, a primary key column" + " named \'rownum\' is not allowed!");
                    }
                }
            }
        }
    } catch (SQLException e) {
        throw new RuntimeException(e);
    } finally {
        try {
            releaseConnection(connection, null, rs);
        } catch (SQLException ignore) {
        } finally {
            try {
                tables.close();
            } catch (SQLException ignore) {
            }
        }
    }
}