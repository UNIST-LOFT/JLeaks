public synchronized boolean isAuthAvailable(String user) 
{
    String sql = "SELECT " + col.NAME + " FROM " + tableName + " WHERE " + col.NAME + "=?;";
    ResultSet rs = null;
    try (Connection con = getConnection();
        PreparedStatement pst = con.prepareStatement(sql)) {
        pst.setString(1, user.toLowerCase());
        rs = pst.executeQuery();
        return rs.next();
    } catch (SQLException ex) {
        logSqlException(ex);
    } finally {
        close(rs);
    }
    return false;
}