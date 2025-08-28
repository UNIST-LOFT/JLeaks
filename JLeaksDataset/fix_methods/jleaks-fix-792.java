private static boolean tableExists(
            DatabaseMetaData dmd, String schema, String table)
            throws SQLException {
        ResultSet rs = dmd.getTables(
                null, schema, table, new String[] {"TABLE"});
        try {
            return rs.next();
        } finally {
            rs.close();
        }
    }