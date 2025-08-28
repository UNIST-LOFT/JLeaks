	static SQLDialect getDialect(DataSource dataSource) {
		try (Connection connection = (dataSource != null) ? dataSource.getConnection() : null) {
			return JDBCUtils.dialect(connection);
		}
		catch (SQLException ex) {
			logger.warn("Unable to determine dialect from datasource", ex);
		}
		return SQLDialect.DEFAULT;
	}
