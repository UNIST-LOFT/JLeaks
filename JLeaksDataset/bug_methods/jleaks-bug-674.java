	public void addIndexes(
			Connection con, String indexesSQL, Set<String> validIndexNames)
		throws IOException {

		if (_log.isInfoEnabled()) {
			_log.info("Adding indexes");
		}

		UnsyncBufferedReader bufferedReader = new UnsyncBufferedReader(
			new UnsyncStringReader(indexesSQL));

		String sql = null;

		while ((sql = bufferedReader.readLine()) != null) {
			if (Validator.isNull(sql)) {
				continue;
			}

			int y = sql.indexOf(" on ");

			int x = sql.lastIndexOf(" ", y - 1);

			String indexName = sql.substring(x + 1, y);

			if (validIndexNames.contains(indexName)) {
				continue;
			}

			if (_log.isInfoEnabled()) {
				_log.info(sql);
			}

			try {
				runSQL(con, sql);
			}
			catch (Exception e) {
				if (_log.isWarnEnabled()) {
					_log.warn(e.getMessage() + ": " + sql);
				}
			}
		}
	}
