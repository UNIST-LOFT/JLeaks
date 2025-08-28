public int getCount() throws SQLException 
{
    // First try the delegate
    int count = countByDelegate();
    if (count < 0) {
        // Couldn't use the delegate, use the bad way.
        Statement statement = null;
        ResultSet rs = null;
        Connection conn = getConnection();
        try {
            statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = statement.executeQuery(queryString);
            if (rs.last()) {
                count = rs.getRow();
            } else {
                count = 0;
            }
        } finally {
            releaseConnection(conn, statement, rs);
        }
    }
    return count;
}