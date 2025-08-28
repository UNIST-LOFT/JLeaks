
    private void readIndexes(DatabaseMetaData meta, HashMap<String, Column> columnMap) {
        String pkName = null;
        try (ResultSet rs = meta.getPrimaryKeys(null, originalSchema, originalTable)) {
            if (rs.next()) {
                pkName = readPrimaryKey(rs, columnMap);
            }
        } catch (Exception e) {
            // Some ODBC bridge drivers don't support it:
            // some combinations of "DataDirect SequeLink(R) for JDBC"
            // http://www.datadirect.com/index.ssp
        }
        try (ResultSet rs = meta.getIndexInfo(null, originalSchema, originalTable, false, true)) {
            readIndexes(rs, columnMap, pkName);
        } catch (Exception e) {
            // Oracle throws an exception if the table is not found or is a
            // SYNONYM
        }
    }