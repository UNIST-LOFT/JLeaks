    private void createEjbTimerDatabaseTable(final String createStatement, final String dbDir) throws Exception {
        checkDerbyDriver();
        final String url = getDatabaseUrl(dbDir);
        final Connection conn = DriverManager.getConnection(url);
        deleteTable(conn);
        final Statement cs = conn.createStatement();
        cs.executeUpdate(createStatement);
        cs.close();
    }
