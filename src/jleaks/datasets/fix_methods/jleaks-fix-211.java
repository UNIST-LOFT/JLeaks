private List<Connection> createConnections(final DataSource dataSource, final int connectionSize) throws SQLException 
{
    boolean hasException = false;
    List<Connection> result = new ArrayList<>(connectionSize);
    for (int i = 0; i < connectionSize; i++) {
        try {
            result.add(dataSource.getConnection());
            // CHECKSTYLE:OFF
        } catch (Exception ex) {
            // CHECKSTYLE:ON
            hasException = true;
        }
    }
    if (hasException) {
        for (Connection each : result) {
            each.close();
        }
        throw new ShardingException(String.format("Could't get %d connections one time, partition succeed connection(%d) have released!", connectionSize, result.size()));
    } else {
        return result;
    }
}