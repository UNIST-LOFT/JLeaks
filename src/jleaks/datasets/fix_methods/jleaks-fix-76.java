private void encryptKeyInKeyStore(Connection conn) 
{
    try (PreparedStatement selectStatement = conn.prepareStatement("SELECT ks.id, ks.key FROM cloud.keystore ks WHERE ks.key IS NOT null");
        ResultSet selectResultSet = selectStatement.executeQuery()) {
        while (selectResultSet.next()) {
            Long keyId = selectResultSet.getLong(1);
            String preSharedKey = selectResultSet.getString(2);
            try (PreparedStatement updateStatement = conn.prepareStatement("UPDATE cloud.keystore ks SET ks.key = ? WHERE ks.id = ?")) {
                updateStatement.setString(1, DBEncryptionUtil.encrypt(preSharedKey));
                updateStatement.setLong(2, keyId);
                updateStatement.executeUpdate();
            }
        }
    } catch (SQLException e) {
        throw new CloudRuntimeException("Exception while encrypting key column in keystore table", e);
    }
    s_logger.debug("Done encrypting keystore's key column");
}