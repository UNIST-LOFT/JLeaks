	public RawResults queryForAllRawOld(ConnectionSource connectionSource, String query) throws SQLException {
		DatabaseConnection connection = connectionSource.getReadOnlyConnection();
		CompiledStatement compiledStatement =
				connection.compileStatement(query, StatementType.SELECT, noFieldTypes);
		try {
			String[] columnNames = extractColumnNames(compiledStatement);
			RawResultsWrapper rawResults =
					new RawResultsWrapper(connectionSource, connection, query, compiledStatement, columnNames, this);
			compiledStatement = null;
			return rawResults;
		} finally {
			if (compiledStatement != null) {
				compiledStatement.close();
			}
		}
	}
