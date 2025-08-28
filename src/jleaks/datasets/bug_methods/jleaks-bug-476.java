    private void readMetaData() throws SQLException {
        DatabaseMetaData meta = conn.getConnection().getMetaData();
        storesLowerCase = meta.storesLowerCaseIdentifiers();
        storesMixedCase = meta.storesMixedCaseIdentifiers();
        storesMixedCaseQuoted = meta.storesMixedCaseQuotedIdentifiers();
        supportsMixedCaseIdentifiers = meta.supportsMixedCaseIdentifiers();
        ArrayList<Column> columnList = Utils.newSmallArrayList();
        HashMap<String, Column> columnMap = new HashMap<>();
        String schema = null;
        boolean isQuery = originalTable.startsWith("(");
        if (!isQuery) {
            ResultSet rs = meta.getTables(null, originalSchema, originalTable, null);
            if (rs.next() && rs.next()) {
                throw DbException.get(ErrorCode.SCHEMA_NAME_MUST_MATCH, originalTable);
            }
            rs.close();
            rs = meta.getColumns(null, originalSchema, originalTable, null);
            int i = 0;
            String catalog = null;
            while (rs.next()) {
                String thisCatalog = rs.getString("TABLE_CAT");
                if (catalog == null) {
                    catalog = thisCatalog;
                }
                String thisSchema = rs.getString("TABLE_SCHEM");
                if (schema == null) {
                    schema = thisSchema;
                }
                if (!Objects.equals(catalog, thisCatalog) ||
                        !Objects.equals(schema, thisSchema)) {
                    // if the table exists in multiple schemas or tables,
                    // use the alternative solution
                    columnMap.clear();
                    columnList.clear();
                    break;
                }
                String n = rs.getString("COLUMN_NAME");
                n = convertColumnName(n);
                int sqlType = rs.getInt("DATA_TYPE");
                String sqlTypeName = rs.getString("TYPE_NAME");
                long precision = rs.getInt("COLUMN_SIZE");
                precision = convertPrecision(sqlType, precision);
                int scale = rs.getInt("DECIMAL_DIGITS");
                scale = convertScale(sqlType, scale);
                int type = DataType.convertSQLTypeToValueType(sqlType, sqlTypeName);
                Column col = new Column(n, TypeInfo.getTypeInfo(type, precision, scale, null));
                col.setTable(this, i++);
                columnList.add(col);
                columnMap.put(n, col);
            }
            rs.close();
        }
        if (originalTable.indexOf('.') < 0 && !StringUtils.isNullOrEmpty(schema)) {
            qualifiedTableName = schema + "." + originalTable;
        } else {
            qualifiedTableName = originalTable;
        }
        // check if the table is accessible

        try (Statement stat = conn.getConnection().createStatement()) {
            ResultSet rs = stat.executeQuery("SELECT * FROM " +
                    qualifiedTableName + " T WHERE 1=0");
            if (columnList.isEmpty()) {
                // alternative solution
                ResultSetMetaData rsMeta = rs.getMetaData();
                for (int i = 0; i < rsMeta.getColumnCount();) {
                    String n = rsMeta.getColumnName(i + 1);
                    n = convertColumnName(n);
                    int sqlType = rsMeta.getColumnType(i + 1);
                    long precision = rsMeta.getPrecision(i + 1);
                    precision = convertPrecision(sqlType, precision);
                    int scale = rsMeta.getScale(i + 1);
                    scale = convertScale(sqlType, scale);
                    int type = DataType.getValueTypeFromResultSet(rsMeta, i + 1);
                    Column col = new Column(n, TypeInfo.getTypeInfo(type, precision, scale, null));
                    col.setTable(this, i++);
                    columnList.add(col);
                    columnMap.put(n, col);
                }
            }
            rs.close();
        } catch (Exception e) {
            throw DbException.get(ErrorCode.TABLE_OR_VIEW_NOT_FOUND_1, e,
                    originalTable + "(" + e.toString() + ")");
        }
        Column[] cols = columnList.toArray(new Column[0]);
        setColumns(cols);
        int id = getId();
        linkedIndex = new LinkedIndex(this, id, IndexColumn.wrap(cols),
                IndexType.createNonUnique(false));
        indexes.add(linkedIndex);
        if (!isQuery) {
            readIndexes(meta, columnMap);
        }
    }
