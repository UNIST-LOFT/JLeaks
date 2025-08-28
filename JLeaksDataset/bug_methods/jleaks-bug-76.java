    private void encryptKeyInKeyStore(Connection conn) {
        PreparedStatement selectStatement = null;
        ResultSet selectResultSet = null;
        PreparedStatement updateStatement = null;
        try {
            selectStatement = conn.prepareStatement("SELECT ks.id, ks.key FROM cloud.keystore ks WHERE ks.key IS NOT null");
            selectResultSet = selectStatement.executeQuery();
            while (selectResultSet.next()) {
                Long keyId = selectResultSet.getLong(1);
                String preSharedKey = selectResultSet.getString(2);
                updateStatement = conn.prepareStatement("UPDATE cloud.keystore ks SET ks.key = ? WHERE ks.id = ?");
                updateStatement.setString(1, DBEncryptionUtil.encrypt(preSharedKey));
                updateStatement.setLong(2, keyId);
                updateStatement.executeUpdate();
                updateStatement.close();
            }
        } catch (SQLException e) {
            throw new CloudRuntimeException("Exception while encrypting key column in keystore table", e);
        } finally {
            if (selectResultSet != null)
                try {
                    selectResultSet.close();
                } catch (SQLException e) {
                }
            if (selectStatement != null)
                try {
                    selectStatement.close();
                } catch (SQLException e) {
                }
            if (updateStatement != null)
                try {
                    updateStatement.close();
                } catch (SQLException e) {
                }
        }
        s_logger.debug("Done encrypting keystore's key column");
    }
