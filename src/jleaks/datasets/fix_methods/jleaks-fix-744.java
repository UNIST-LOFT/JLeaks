public static LibreBlock getLatestForTrend(long start_time, long end_time) 
{
    SQLiteDatabase db = Cache.openDatabase();
    // Using this syntex since there is no way to tell the DB which index to use.
    // Using ActiveAndroid method would take up to 8 seconds to complete.
    try (Cursor cursor = db.rawQuery("select * from libreblock  INDEXED BY  index_LibreBlock_timestamp " + "where bytestart == 0 AND (byteend == 344 OR byteend == 44) " + "AND timestamp >= " + start_time + " AND timestamp <= " + end_time + " ORDER BY timestamp desc limit 1", null)) {
        if (cursor == null || cursor.getCount() == 0) {
            return null;
        }
        cursor.moveToFirst();
        LibreBlock libreBlock = getFromCursor(cursor);
        return libreBlock;
    }
}