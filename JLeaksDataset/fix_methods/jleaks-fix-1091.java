
	public synchronized RecordHistory read(int historyId) throws HttpMalformedHeaderException, SQLException {
	    psRead.setInt(1, historyId);
		psRead.execute();
		ResultSet rs = psRead.getResultSet();
		RecordHistory result = null;
		try {
			result = build(rs);
		} finally {
			rs.close();
		}

		return result;
	}
	