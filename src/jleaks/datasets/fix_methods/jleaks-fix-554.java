private static String buildDataBaseVersion() 
{
    final StringBuilder result = new StringBuilder();
    try {
        // on commence par voir si le driver jdbc a été utilisé
        // car s'il n'y a pas de datasource une exception est déclenchée
        final JdbcDriver jdbcDriver = JdbcDriver.SINGLETON;
        if (jdbcDriver.getLastConnectUrl() != null) {
            final Connection connection = DriverManager.getConnection(jdbcDriver.getLastConnectUrl(), jdbcDriver.getLastConnectInfo());
            connection.setAutoCommit(false);
            try {
                appendDataBaseVersion(result, connection);
            } finally {
                try {
                    connection.rollback();
                } finally {
                    connection.close();
                }
            }
            return result.toString();
        }
        // on cherche une datasource avec InitialContext pour afficher nom et version bdd + nom et version driver jdbc
        // (le nom de la dataSource recherchée dans JNDI est du genre jdbc/Xxx qui est le nom standard d'une DataSource)
        final Map<String, DataSource> dataSources = JdbcWrapperHelper.getJndiAndSpringDataSources();
        for (final Map.Entry<String, DataSource> entry : dataSources.entrySet()) {
            final String name = entry.getKey();
            final DataSource dataSource = entry.getValue();
            final Connection connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            try {
                if (result.length() > 0) {
                    result.append("\n\n");
                }
                result.append(name).append(":\n");
                appendDataBaseVersion(result, connection);
            } finally {
                try {
                    connection.rollback();
                } finally {
                    connection.close();
                }
            }
        }
    } catch (final NamingException e) {
        result.append(e.toString());
    } catch (final SQLException e) {
        result.append(e.toString());
    }
    if (result.length() > 0) {
        return result.toString();
    }
    return null;
}