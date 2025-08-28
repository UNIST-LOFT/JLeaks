    public DataSource get() {
        HikariDataSource dataSource = new HikariDataSource(createConfiguration());
        flywayMigrate(dataSource);

        return dataSource;
    }
