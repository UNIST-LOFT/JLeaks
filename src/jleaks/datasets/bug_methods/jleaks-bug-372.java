    public static Connection getConnection(String url, String username, String password) throws SQLException {
        Connection dbConn = getConnectionInternal(url, username, password);
        boolean failed = true;
        if (url.startsWith("jdbc:mysql")) {
            for(int i = 0; i < MAX_RETRY_TIMES && failed; ++i) {
                try {
                    dbConn.createStatement().execute("select 111");
                    failed = false;
                } catch(SQLException e) {
                    if(i == MAX_RETRY_TIMES) {
                        throw e;
                    } else {
                        SysUtil.sleep(3000);
                    }
                }
            }
        }

        return dbConn;
    }
