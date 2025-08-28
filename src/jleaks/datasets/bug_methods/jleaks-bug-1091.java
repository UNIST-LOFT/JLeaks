	public synchronized RecordHistory read(int historyId) throws HttpMalformedHeaderException, SQLException {
	    psRead.setInt(1, historyId);
		psRead.execute();
		ResultSet rs = psRead.getResultSet();
		RecordHistory result = build(rs);
		rs.close();

		return result;
	}
