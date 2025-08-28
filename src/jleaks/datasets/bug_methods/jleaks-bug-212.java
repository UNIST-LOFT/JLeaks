    private List<Connection> createConnections(final DataSource dataSource, final int connectionSize) throws SQLException {
        List<Connection> result = new ArrayList<>(connectionSize);
        for (int i = 0; i < connectionSize; i++) {
            result.add(createConnection(dataSource));
        }
        return result;
    }
