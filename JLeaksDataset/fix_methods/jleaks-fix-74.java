public boolean update(String name, String value) 
{
    TransactionLegacy txn = TransactionLegacy.currentTxn();
    PreparedStatement stmt = null;
    try {
        value = ("Hidden".equals(category) || "Secure".equals(category)) ? DBEncryptionUtil.encrypt(value) : value;
        stmt = txn.prepareStatement(UPDATE_CONFIGURATION_SQL);
        stmt.setString(1, value);
        stmt.setString(2, name);
        stmt.executeUpdate();
        return true;
    } catch (Exception e) {
        s_logger.warn("Unable to update Configuration Value", e);
    } finally {
        try {
            if (stmt != null)
                stmt.close();
        } catch (SQLException e) {
        }
    }
    return false;
}