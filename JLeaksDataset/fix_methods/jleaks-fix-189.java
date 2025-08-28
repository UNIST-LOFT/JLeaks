public void testDebugLogs() throws Exception 
{
    String tableName = generateUniqueName();
    createTableAndInsertValues(tableName, true);
    Properties props = new Properties();
    props.setProperty(QueryServices.LOG_LEVEL, LogLevel.DEBUG.name());
    Connection conn = DriverManager.getConnection(getUrl(), props);
    assertEquals(conn.unwrap(PhoenixConnection.class).getLogLevel(), LogLevel.DEBUG);
    String query = "SELECT * FROM " + tableName;
    StatementContext context;
    try (ResultSet rs = conn.createStatement().executeQuery(query)) {
        context = ((PhoenixResultSet) rs).getContext();
        while (rs.next()) {
            rs.getString(1);
            rs.getString(2);
        }
    }
    String queryId = context.getQueryLogger().getQueryId();
    String logQuery = "SELECT * FROM " + SYSTEM_CATALOG_SCHEMA + ".\"" + SYSTEM_LOG_TABLE + "\"";
    int delay = 5000;
    // sleep for sometime to let query log committed
    Thread.sleep(delay);
    try (ResultSet explainRS = conn.createStatement().executeQuery("Explain " + query);
        ResultSet rs = conn.createStatement().executeQuery(logQuery)) {
        boolean foundQueryLog = false;
        while (rs.next()) {
            if (rs.getString(QUERY_ID).equals(queryId)) {
                foundQueryLog = true;
                assertEquals(rs.getString(BIND_PARAMETERS), null);
                assertEquals(rs.getString(USER), System.getProperty("user.name"));
                assertEquals(rs.getString(CLIENT_IP), InetAddress.getLocalHost().getHostAddress());
                assertEquals(rs.getString(EXPLAIN_PLAN), QueryUtil.getExplainPlan(explainRS));
                assertEquals(rs.getString(GLOBAL_SCAN_DETAILS), context.getScan().toJSON());
                assertEquals(rs.getLong(NO_OF_RESULTS_ITERATED), 10);
                assertEquals(rs.getString(QUERY), query);
                assertEquals(rs.getString(QUERY_STATUS), QueryStatus.COMPLETED.toString());
                assertEquals(rs.getString(TENANT_ID), null);
                assertTrue(rs.getString(SCAN_METRICS_JSON) == null);
                assertEquals(rs.getString(EXCEPTION_TRACE), null);
            } else {
                // confirm we are not logging system queries
                assertFalse(rs.getString(QUERY).toString().contains(SYSTEM_CATALOG_SCHEMA));
            }
        }
        assertTrue(foundQueryLog);
        conn.close();
    }
}