    public static void deleteUndoLog(String xid, long branchId, Connection conn) throws SQLException {
        PreparedStatement deletePST = conn.prepareStatement(DELETE_UNDO_LOG_SQL);
        deletePST.setLong(1, branchId);
        deletePST.setString(2, xid);
        deletePST.executeUpdate();
    }
