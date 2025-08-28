public synchronized RecordAlert read(int alertId) throws SQLException 
{
    psRead.setInt(1, alertId);
    ResultSet rs = psRead.executeQuery();
    try {
        RecordAlert ra = build(rs);
        return ra;
    } finally {
        rs.close();
    }
}