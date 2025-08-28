public static Connection getConnection(String url, String username, String password) throws SQLException 
{
    Connection dbConn;
    synchronized (ClassUtil.lock_str) {
        DriverManager.setLoginTimeout(10);
        if (username == null) {
            dbConn = DriverManager.getConnection(url);
        } else {
            dbConn = DriverManager.getConnection(url, username, password);
        }
    }
    return dbConn;
}