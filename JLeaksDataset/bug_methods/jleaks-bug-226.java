    public synchronized boolean isAuthAvailable(String user) {
        try (Connection con = getConnection()) {
            String sql = "SELECT " + col.NAME + " FROM " + tableName + " WHERE " + col.NAME + "=?;";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, user.toLowerCase());
            ResultSet rs = pst.executeQuery();
            return rs.next();
        } catch (SQLException ex) {
            logSqlException(ex);
        }
        return false;
    }
