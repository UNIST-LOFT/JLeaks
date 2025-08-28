    public int getCount() throws SQLException {
        // First try the delegate
        int count = countByDelegate();
        if (count < 0) {
            // Couldn't use the delegate, use the bad way.
            Connection conn = getConnection();
            Statement statement = conn.createStatement(
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);

            ResultSet rs = statement.executeQuery(queryString);
            if (rs.last()) {
                count = rs.getRow();
            } else {
                count = 0;
            }
            rs.close();
            statement.close();
            releaseConnection(conn);
        }
        return count;
    }
