public void populateTable(Connection con) throws Exception 
{
    if (_tempFileName == null) {
        return;
    }
    try (PreparedStatement ps = con.prepareStatement(getInsertSQL());
        UnsyncBufferedReader unsyncBufferedReader = new UnsyncBufferedReader(new FileReader(_tempFileName))) {
        DatabaseMetaData databaseMetaData = con.getMetaData();
        if (!databaseMetaData.supportsBatchUpdates()) {
            if (_log.isDebugEnabled()) {
                _log.debug("Database does not support batch updates");
            }
        }
        int count = 0;
        String line = null;
        while ((line = unsyncBufferedReader.readLine()) != null) {
            String[] values = StringUtil.split(line);
            Object[][] columns = getColumns();
            if (values.length != columns.length) {
                throw new UpgradeException("Column lengths differ between temp file and schema. " + "Attempted to insert row " + line + ".");
            }
            int[] order = getOrder();
            for (int i = 0; i < order.length; i++) {
                int pos = order[i];
                setColumn(ps, i, (Integer) columns[pos][1], values[pos]);
            }
            if (databaseMetaData.supportsBatchUpdates()) {
                ps.addBatch();
                if (count == _BATCH_SIZE) {
                    ps.executeBatch();
                    count = 0;
                } else {
                    count++;
                }
            } else {
                ps.executeUpdate();
            }
        }
        if (count != 0) {
            ps.executeBatch();
        }
    }
    if (_log.isDebugEnabled()) {
        _log.debug(getTableName() + " table populated with data");
    }
}