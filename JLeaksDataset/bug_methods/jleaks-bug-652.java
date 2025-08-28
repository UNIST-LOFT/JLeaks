    private ArrayList<Long> deleteEmptyFolders() {
        ArrayList<Long> folderIds = new ArrayList<>();
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            // Select folders whose id do not match any container value.
            String selection = LauncherSettings.Favorites.ITEM_TYPE + " = "
                    + LauncherSettings.Favorites.ITEM_TYPE_FOLDER + " AND "
                    + LauncherSettings.Favorites._ID +  " NOT IN (SELECT " +
                            LauncherSettings.Favorites.CONTAINER + " FROM "
                                + Favorites.TABLE_NAME + ")";
            Cursor c = db.query(Favorites.TABLE_NAME,
                    new String[] {LauncherSettings.Favorites._ID},
                    selection, null, null, null, null);
            while (c.moveToNext()) {
                folderIds.add(c.getLong(0));
            }
            c.close();
            if (!folderIds.isEmpty()) {
                db.delete(Favorites.TABLE_NAME, Utilities.createDbSelectionQuery(
                        LauncherSettings.Favorites._ID, folderIds), null);
            }
            db.setTransactionSuccessful();
        } catch (SQLException ex) {
            Log.e(TAG, ex.getMessage(), ex);
            folderIds.clear();
        } finally {
            db.endTransaction();
        }
        return folderIds;
    }
