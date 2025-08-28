private static Statement executeQueryWithResults(Connection conn, String qry) throws SQLException 
{
    Statement stmnt = null;
    try {
        stmnt = conn.createStatement();
        stmnt.executeQuery(qry);
        SQLWarning warn = stmnt.getWarnings();
        if (warn != null) {
            LOGGER.warn(warn);
        }
        return stmnt;
    } catch (SQLException rethrow) {
        // in case of exception, try to close the statement to avoid a resource leak...
        if (stmnt != null) {
            stmnt.close();
        }
        // ... and rethrow the exception
        throw rethrow;
    }
}