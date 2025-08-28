    private void readIndexes(DatabaseMetaData meta, HashMap<String, Column> columnMap) throws SQLException {
        ResultSet rs;
        try {
            rs = meta.getPrimaryKeys(null, originalSchema, originalTable);
        } catch (Exception e) {
            // Some ODBC bridge drivers don't support it:
            // some combinations of "DataDirect SequeLink(R) for JDBC"
            // http://www.datadirect.com/index.ssp
            rs = null;
        }
        String pkName = null;
        ArrayList<Column> list;
        if (rs != null && rs.next()) {
            // the problem is, the rows are not sorted by KEY_SEQ
            list = Utils.newSmallArrayList();
            do {
                int idx = rs.getInt("KEY_SEQ");
                if (StringUtils.isNullOrEmpty(pkName)) {
                    pkName = rs.getString("PK_NAME");
                }
                while (list.size() < idx) {
                    list.add(null);
                }
                String col = rs.getString("COLUMN_NAME");
                col = convertColumnName(col);
                Column column = columnMap.get(col);
                if (idx == 0) {
                    // workaround for a bug in the SQLite JDBC driver
                    list.add(column);
                } else {
                    list.set(idx - 1, column);
                }
            } while (rs.next());
            addIndex(list, IndexType.createPrimaryKey(false, false));
            rs.close();
        }
        try {
            rs = meta.getIndexInfo(null, originalSchema, originalTable, false, true);
        } catch (Exception e) {
            // Oracle throws an exception if the table is not found or is a
            // SYNONYM
            rs = null;
        }
        String indexName = null;
        list = Utils.newSmallArrayList();
        IndexType indexType = null;
        if (rs != null) {
            while (rs.next()) {
                if (rs.getShort("TYPE") == DatabaseMetaData.tableIndexStatistic) {
                    // ignore index statistics
                    continue;
                }
                String newIndex = rs.getString("INDEX_NAME");
                if (pkName != null && pkName.equals(newIndex)) {
                    continue;
                }
                if (indexName != null && !indexName.equals(newIndex)) {
                    addIndex(list, indexType);
                    indexName = null;
                }
                if (indexName == null) {
                    indexName = newIndex;
                    list.clear();
                }
                boolean unique = !rs.getBoolean("NON_UNIQUE");
                indexType = unique ? IndexType.createUnique(false, false) :
                        IndexType.createNonUnique(false);
                String col = rs.getString("COLUMN_NAME");
                col = convertColumnName(col);
                Column column = columnMap.get(col);
                list.add(column);
            }
            rs.close();
        }
        if (indexName != null) {
            addIndex(list, indexType);
        }
    }
