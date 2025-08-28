    public static TableDataResponse getTableData(SQLiteDatabase db, String selectQuery, String tableName) {

        TableDataResponse tableData = new TableDataResponse();
        tableData.isSelectQuery = true;
        if (tableName == null) {
            tableName = getTableName(selectQuery);
        }

        if (tableName != null) {
            final String pragmaQuery = "PRAGMA table_info(" + tableName + ")";
            tableData.tableInfos = getTableInfo(db, pragmaQuery);
        }
        Cursor cursor;
        boolean isView = false;
        try {
            cursor = db.rawQuery("SELECT type FROM sqlite_master WHERE name=?", new String[]{tableName});
            if (cursor.moveToFirst()) {
                isView = "view".equalsIgnoreCase(cursor.getString(0));
            }
        } catch (Exception e) {}
        tableData.isEditable = tableName != null && tableData.tableInfos != null && !isView;

        try {
            cursor = db.rawQuery(selectQuery, null);
        } catch (Exception e) {
            e.printStackTrace();
            tableData.isSuccessful = false;
            tableData.errorMessage = e.getMessage();
            return tableData;
        }

        if (cursor != null) {
            cursor.moveToFirst();

            // setting tableInfo when tableName is not known and making
            // it non-editable also by making isPrimary true for all
            if (tableData.tableInfos == null) {
                tableData.tableInfos = new ArrayList<>();
                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    TableDataResponse.TableInfo tableInfo = new TableDataResponse.TableInfo();
                    tableInfo.title = cursor.getColumnName(i);
                    tableInfo.isPrimary = true;
                    tableData.tableInfos.add(tableInfo);
                }
            }

            tableData.isSuccessful = true;
            tableData.rows = new ArrayList<>();
            if (cursor.getCount() > 0) {

                do {
                    List<TableDataResponse.ColumnData> row = new ArrayList<>();
                    for (int i = 0; i < cursor.getColumnCount(); i++) {
                        TableDataResponse.ColumnData columnData = new TableDataResponse.ColumnData();
                        switch (cursor.getType(i)) {
                            case Cursor.FIELD_TYPE_BLOB:
                                columnData.dataType = DataType.TEXT;
                                columnData.value = ConverterUtils.blobToString(cursor.getBlob(i));
                                break;
                            case Cursor.FIELD_TYPE_FLOAT:
                                columnData.dataType = DataType.REAL;
                                columnData.value = cursor.getDouble(i);
                                break;
                            case Cursor.FIELD_TYPE_INTEGER:
                                columnData.dataType = DataType.INTEGER;
                                columnData.value = cursor.getLong(i);
                                break;
                            case Cursor.FIELD_TYPE_STRING:
                                columnData.dataType = DataType.TEXT;
                                columnData.value = cursor.getString(i);
                                break;
                            default:
                                columnData.dataType = DataType.TEXT;
                                columnData.value = cursor.getString(i);
                        }
                        row.add(columnData);
                    }
                    tableData.rows.add(row);

                } while (cursor.moveToNext());
            }
            cursor.close();
            return tableData;
        } else {
            tableData.isSuccessful = false;
            tableData.errorMessage = "Cursor is null";
            return tableData;
        }

    }
