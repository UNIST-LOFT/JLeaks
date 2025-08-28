public HashedPassword getPassword(final String userName) 
{
    final String sql = "select password from ta_users where username = ?";
    final Connection con = connectionSupplier.get();
    try {
        try (final PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, userName);
            try (final ResultSet rs = ps.executeQuery()) {
                String returnValue = null;
                if (rs.next()) {
                    returnValue = rs.getString(1);
                }
                return returnValue == null ? null : new HashedPassword(returnValue);
            }
        }
    } catch (final SQLException sqle) {
        throw new IllegalStateException("Error getting password for user:" + userName, sqle);
    } finally {
        DbUtil.closeConnection(con);
    }
}