public void insertOp(ObjectProtection objProt, TransactionMgr transMgr) throws SQLException{
    try (PreparedStatement stmt = transMgr.dbCon.prepareStatement(OP_INSERT)) {
        stmt.setString(1, objProt.getObjectProtectionPath());
        stmt.setString(2, objProt.getCreator().name.value);
        stmt.setString(3, objProt.getOwner().name.value);
        stmt.setString(4, objProt.getSecurityType().name.value);
        stmt.executeUpdate();
    }
}