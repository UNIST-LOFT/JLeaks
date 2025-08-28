public DataSource get() 
{
    HikariDataSource dataSource = null;
    try {
        dataSource = new HikariDataSource(createConfiguration());
        flywayMigrate(dataSource);
        return dataSource;
    } catch (final Throwable t) {
        if (null != dataSource && !dataSource.isClosed()) {
            dataSource.close();
        }
        logger.error("error migration DB", t);
        throw t;
    }
}