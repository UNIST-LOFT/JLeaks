	public void execute(Connection conn) throws SQLException {
		mergeCondition();
		updateCount = -1;
		try {
			// SELECT ...
			if (sql.isSELECT()) {
				// If without callback, the query do NOT make sense.
				if (null != callback) {
					// Create ResultSet type upon the page. default is
					// TYPE_FORWARD_ONLY
					Pager pager = context.getPager();
					int rsType = null == pager ? ResultSet.TYPE_FORWARD_ONLY : pager
							.getResultSetType();

					// Prepare statment for query
					PreparedStatement stat = conn.prepareStatement(sql.toPreparedStatementString(),
							rsType, ResultSet.CONCUR_READ_ONLY);

					// Put all parameters to PreparedStatement and get ResultSet
					adapter.process(stat, sql, entity);
					ResultSet rs = stat.executeQuery();

					// Get result from ResultSet by callback
					context.setResult(callback.invoke(conn, rs, this));

					// Closing...
					rs.close();
					stat.close();
				}
			}
			// UPDATE | INSERT | DELETE | TRUNCATE ...
			else if (sql.isUPDATE() || sql.isINSERT() || sql.isDELETE() || sql.isTRUNCATE()) {
				PreparedStatement stat = conn.prepareStatement(sql.toPreparedStatementString());
				adapter.process(stat, sql, entity);
				stat.execute();
				updateCount = stat.getUpdateCount();
				stat.close();
				if (null != callback)
					context.setResult(callback.invoke(conn, null, this));
			}
			// CREATE | DROP
			else {
				Statement stat = conn.createStatement();
				stat.execute(sql.toString());
				stat.close();
				if (null != callback)
					context.setResult(callback.invoke(conn, null, this));
			}
		}
		// If any SQLException happend, throw out the SQL string
		catch (SQLException e) {
			throw new SQLException(format("!Nuz SQL Error: '%s'", sql.toString()), e);
		}

	}
