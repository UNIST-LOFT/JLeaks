    private static Statement executeQueryWithResults(Connection conn, String qry) throws SQLException {
        Statement stmnt = conn.createStatement();
        stmnt.executeQuery(qry);
        SQLWarning warn = stmnt.getWarnings();
        if (warn != null) {
            LOGGER.warn(warn);
        }
        return stmnt;
    }