    private void persistImpl(String instanceName, String key, String value, TransactionMgr transMgr) throws SQLException
    {
        PreparedStatement stmt = transMgr.dbCon.prepareStatement(
            SELECT_ENTRY_FOR_UPDATE,
            ResultSet.TYPE_SCROLL_SENSITIVE,
            ResultSet.CONCUR_UPDATABLE
        );

        String instanceUpper = instanceName.toUpperCase();
        stmt.setString(1, instanceUpper);
        stmt.setString(2, key);

        ResultSet resultSet = stmt.executeQuery();
        if (resultSet.next())
        {
            resultSet.updateString(3, value);
            resultSet.updateRow();
        }
        else
        {
            resultSet.moveToInsertRow();
            resultSet.updateString(1, instanceUpper);
            resultSet.updateString(2, key);
            resultSet.updateString(3, value);
            resultSet.insertRow();
        }

        resultSet.close();
        stmt.close();
    }
