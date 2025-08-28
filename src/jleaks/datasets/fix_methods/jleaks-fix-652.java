private ArrayList<Long> deleteEmptyFolders() 
{
    ArrayList<Long> folderIds = new ArrayList<>();
    SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    try (SQLiteTransaction t = new SQLiteTransaction(db)) {
        // Select folders whose id do not match any container value.
        String selection = LauncherSettings.Favorites.ITEM_TYPE + " = " + LauncherSettings.Favorites.ITEM_TYPE_FOLDER + " AND " + LauncherSettings.Favorites._ID + " NOT IN (SELECT " + LauncherSettings.Favorites.CONTAINER + " FROM " + Favorites.TABLE_NAME + ")";
        try (Cursor c = db.query(Favorites.TABLE_NAME, new String[] { LauncherSettings.Favorites._ID }, selection, null, null, null, null)) {
            LauncherDbUtils.iterateCursor(c, 0, folderIds);
        }
        if (!folderIds.isEmpty()) {
            db.delete(Favorites.TABLE_NAME, Utilities.createDbSelectionQuery(LauncherSettings.Favorites._ID, folderIds), null);
        }
        t.commit();
    } catch (SQLException ex) {
        Log.e(TAG, ex.getMessage(), ex);
        folderIds.clear();
    }
    return folderIds;
}