public int getRows(String tableName) throws NamingException, SQLException 
{
    Context ctx = cache.getJNDIContext();
    DataSource ds = (DataSource) ctx.lookup("java:/SimpleDataSource");
    String sql = "select * from " + tableName;
    int counter = 0;
    try (Connection conn = ds.getConnection();
        Statement sm = conn.createStatement();
        ResultSet rs = sm.executeQuery(sql)) {
        while (rs.next()) {
            counter++;
        }
    }
    return counter;
}