public void destroy() throws Exception 
{
    if (dataSourceRequiresShutdown()) {
        try (Connection connection = this.dataSource.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("SHUTDOWN");
            }
        }
    }
}