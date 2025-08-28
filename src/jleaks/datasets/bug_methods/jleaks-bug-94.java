  public int getRows(String tableName) throws NamingException, SQLException {

    Context ctx = cache.getJNDIContext();
    DataSource ds = (DataSource) ctx.lookup("java:/SimpleDataSource");

    String sql = "select * from " + tableName;

    Connection conn = ds.getConnection();
    Statement sm = conn.createStatement();
    ResultSet rs = sm.executeQuery(sql);
    int counter = 0;
    while (rs.next()) {
      counter++;
      // System.out.println("id "+rs.getString(1)+ " name "+rs.getString(2));
    }
    rs.close();
    conn.close();

    return counter;
  }
