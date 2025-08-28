	public synchronized RecordAlert read(int alertId) throws SQLException {
		psRead.setInt(1, alertId);
		ResultSet rs = psRead.executeQuery();
		RecordAlert ra = build(rs);
		rs.close();
		return ra;
	}
