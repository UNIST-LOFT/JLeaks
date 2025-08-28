    public void insertContents(Content[] rows) {
        synchronized (locker) {
            Log.i(TAG, "insertContents");
            SQLiteDatabase db = null;
            SQLiteStatement statement = null;
            try {
                db = this.getWritableDatabase();
                statement = db.compileStatement(Content.INSERT_STATEMENT);
                db.beginTransaction();
                for (Content row : rows) {

                    deleteContent(db, row);

                    statement.clearBindings();
                    statement.bindLong(1, row.getId());
                    statement.bindString(2, row.getUniqueSiteId());
                    String category = row.getCategory();
                    if (category == null)
                        statement.bindNull(3);
                    else
                        statement.bindString(3, category);
                    statement.bindString(4, row.getUrl());
                    if (row.getHtmlDescription() == null)
                        statement.bindNull(5);
                    else
                        statement.bindString(5, row.getHtmlDescription());
                    if (row.getTitle() == null)
                        statement.bindNull(6);
                    else
                        statement.bindString(6, row.getTitle());
                    statement.bindLong(7, row.getQtyPages());
                    statement.bindLong(8, row.getUploadDate());
                    statement.bindLong(9, row.getDownloadDate());
                    statement.bindLong(10, row.getStatus().getCode());
                    if (row.getCoverImageUrl() == null)
                        statement.bindNull(11);
                    else
                        statement.bindString(11, row.getCoverImageUrl());
                    statement.bindLong(12, row.getSite().getCode());
                    statement.execute();

                    if (row.getImageFiles() != null)
                        insertImageFiles(db, row);

                    List<Attribute> attributes = new ArrayList<>();
                    for (AttributeType attributeType : AttributeType.values()) {
                        if (row.getAttributes().get(attributeType) != null) {
                            attributes.addAll(row.getAttributes().get(attributeType));
                        }
                    }
                    insertAttributes(db, row, attributes);
                }
                db.setTransactionSuccessful();
                db.endTransaction();

            } finally {
                Log.i(TAG, "insertContents - trying to close the db connection. Condition : " + (db != null && db.isOpen()));
                if (statement != null)
                    statement.close();
                if (db != null && db.isOpen())
                    db.close(); // Closing database connection
            }
        }
    }
