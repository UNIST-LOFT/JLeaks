    public void testGetConnectionsThrottledForSameUrl() throws Exception {
        int expectedPhoenixConnections = 11;
        List<Connection> connections = Lists.newArrayList();
        String zkQuorum = "localhost:" + getUtility().getZkCluster().getClientPort();
        String url = PhoenixRuntime.JDBC_PROTOCOL + PhoenixRuntime.JDBC_PROTOCOL_SEPARATOR + zkQuorum +
        ':' +  CUSTOM_URL_STRING + '=' + "throttletest";

        Properties props = new Properties();
        props.setProperty(QueryServices.CLIENT_CONNECTION_MAX_ALLOWED_CONNECTIONS, "10");

        GLOBAL_HCONNECTIONS_COUNTER.getMetric().reset();
        GLOBAL_QUERY_SERVICES_COUNTER.getMetric().reset();
        GLOBAL_PHOENIX_CONNECTIONS_ATTEMPTED_COUNTER.getMetric().reset();
        GLOBAL_PHOENIX_CONNECTIONS_THROTTLED_COUNTER.getMetric().reset();
        boolean wasThrottled = false;
        try {
            for (int k = 0; k < expectedPhoenixConnections; k++) {
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
        assertEquals(expectedPhoenixConnections, GLOBAL_PHOENIX_CONNECTIONS_ATTEMPTED_COUNTER.getMetric().getValue());
    }
