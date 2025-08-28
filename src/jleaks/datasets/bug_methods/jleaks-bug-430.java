    public void initDatabaseType() {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            String databaseProductName = databaseMetaData.getDatabaseProductName();
            logger.debug("database product name: '{}'", databaseProductName);

            // CRDB does not expose the version through the jdbc driver, so we need to fetch it through version().
            if (PRODUCT_NAME_POSTGRES.equalsIgnoreCase(databaseProductName)) {
                PreparedStatement preparedStatement = connection.prepareStatement("select version() as version;");
                ResultSet resultSet = preparedStatement.executeQuery();
                String version = null;
                if (resultSet.next()) {
                    version = resultSet.getString("version");
                }
                resultSet.close();

                if (StringUtils.isNotEmpty(version) && version.toLowerCase().startsWith(PRODUCT_NAME_CRDB.toLowerCase())) {
                    databaseProductName = PRODUCT_NAME_CRDB;
                    logger.info("CockroachDB version '{}' detected", version);
                }
            }

            databaseType = databaseTypeMappings.getProperty(databaseProductName);
            if (databaseType == null) {
                throw new FlowableException("couldn't deduct database type from database product name '" + databaseProductName + "'");
            }
            logger.debug("using database type: {}", databaseType);

        } catch (SQLException e) {
            logger.error("Exception while initializing Database connection", e);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                logger.error("Exception while closing the Database connection", e);
            }
        }

        // Special care for MSSQL, as it has a hard limit of 2000 params per statement (incl bulk statement).
        // Especially with executions, with 100 as default, this limit is passed.
        if (DATABASE_TYPE_MSSQL.equals(databaseType)) {
            maxNrOfStatementsInBulkInsert = DEFAULT_MAX_NR_OF_STATEMENTS_BULK_INSERT_SQL_SERVER;
        }
    }
