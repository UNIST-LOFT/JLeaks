private static String getDatabaseName(Configuration configuration, Database database) 
{
    try {
        return database.getCatalog();
    } catch (Exception e) {
        try (Connection connection = configuration.getDataSource().getConnection()) {
            String catalog = connection.getCatalog();
            return catalog != null ? catalog : "";
        } catch (Exception e1) {
            return "";
        }
    }
}