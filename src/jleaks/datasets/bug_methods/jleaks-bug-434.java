    private static String getDatabaseName(Configuration configuration, Database database) {
        try {
            Connection connection = configuration.getDataSource().getConnection();
            String catalog = connection.getCatalog();
            connection.close();
            return catalog != null ? catalog : database.getCatalog();
        } catch (Exception e) {
            return "";
        }
    }
