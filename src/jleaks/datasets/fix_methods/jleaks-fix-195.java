public void testGetConnectionsThrottledForSameUrl() throws Exception 
{
    int attemptedPhoenixConnections = 11;
    int maxConnections = attemptedPhoenixConnections - 1;
    List<Connection> connections = Lists.newArrayList();
    String zkQuorum = "localhost:" + getUtility().getZkCluster().getClientPort();
    String url = PhoenixRuntime.JDBC_PROTOCOL + PhoenixRuntime.JDBC_PROTOCOL_SEPARATOR + zkQuorum + ':' + CUSTOM_URL_STRING + '=' + "throttletest";
    Properties props = new Properties();
    props.setProperty(QueryServices.CLIENT_CONNECTION_MAX_ALLOWED_CONNECTIONS, Integer.toString(maxConnections));
    GLOBAL_HCONNECTIONS_COUNTER.getMetric().reset();
    GLOBAL_QUERY_SERVICES_COUNTER.getMetric().reset();
    GLOBAL_PHOENIX_CONNECTIONS_ATTEMPTED_COUNTER.getMetric().reset();
    GLOBAL_PHOENIX_CONNECTIONS_THROTTLED_COUNTER.getMetric().reset();
    boolean wasThrottled = false;
    try {
        for (int k = 0; k < attemptedPhoenixConnections; k++) {
            connections.add(DriverManager.getConnection(url, props));
        }
    } catch (SQLException se) {
        wasThrottled = true;
        assertEquals(SQLExceptionCode.NEW_CONNECTION_THROTTLED.getErrorCode(), se.getErrorCode());
    } finally {
        for (Connection c : connections) {
            c.close();
        }
    }
    assertEquals(1, GLOBAL_QUERY_SERVICES_COUNTER.getMetric().getValue());
    assertTrue("No connection was throttled!", wasThrottled);
    assertEquals(1, GLOBAL_PHOENIX_CONNECTIONS_THROTTLED_COUNTER.getMetric().getValue());
    assertEquals(maxConnections, connections.size());
    assertTrue("Not all connections were attempted!", attemptedPhoenixConnections <= GLOBAL_PHOENIX_CONNECTIONS_ATTEMPTED_COUNTER.getMetric().getValue());
    connections.clear();
    // now check that we decremented the counter for the connections we just released
    try {
        for (int k = 0; k < maxConnections; k++) {
            connections.add(DriverManager.getConnection(url, props));
        }
    } catch (SQLException se) {
        if (se.getErrorCode() == (SQLExceptionCode.NEW_CONNECTION_THROTTLED).getErrorCode()) {
            fail("Connection was throttled when it shouldn't be!");
        }
    } finally {
        for (Connection c : connections) {
            c.close();
        }
    }
    assertEquals(maxConnections, connections.size());
}