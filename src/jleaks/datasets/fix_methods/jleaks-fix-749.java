public static void restore(Context context) 
{
    if (restored)
        return;
    restored = true;
    OneSignalDbHelper dbHelper = OneSignalDbHelper.getInstance(context);
    SQLiteDatabase writableDb = dbHelper.getWritableDatabase();
    writableDb.beginTransaction();
    try {
        NotificationBundleProcessor.deleteOldNotifications(writableDb);
        writableDb.setTransactionSuccessful();
    } catch (Throwable t) {
        OneSignal.Log(OneSignal.LOG_LEVEL.ERROR, "Error deleting old notification records! ", t);
    } finally {
        writableDb.endTransaction();
    }
    String[] retColumn = { NotificationTable.COLUMN_NAME_ANDROID_NOTIFICATION_ID, NotificationTable.COLUMN_NAME_FULL_DATA };
    SQLiteDatabase readableDb = dbHelper.getReadableDatabase();
    Cursor cursor = readableDb.query(NotificationTable.TABLE_NAME, retColumn, // 1 Week back.
    NotificationTable.COLUMN_NAME_CREATED_TIME + " > " + ((System.currentTimeMillis() / 1000L) - 604800L) + " AND " + NotificationTable.COLUMN_NAME_DISMISSED + " = 0 AND " + NotificationTable.COLUMN_NAME_OPENED + " = 0 AND " + NotificationTable.COLUMN_NAME_IS_SUMMARY + " = 0", null, // group by
    null, // filter by row groups
    null, // sort order, old to new
    NotificationTable._ID + " ASC");
    try {
        if (cursor.moveToFirst()) {
            boolean useExtender = (NotificationExtenderService.getIntent(context) != null);
            do {
                int existingId = cursor.getInt(cursor.getColumnIndex(NotificationTable.COLUMN_NAME_ANDROID_NOTIFICATION_ID));
                String fullData = cursor.getString(cursor.getColumnIndex(NotificationTable.COLUMN_NAME_FULL_DATA));
                Intent serviceIntent;
                if (useExtender)
                    serviceIntent = NotificationExtenderService.getIntent(context);
                else
                    serviceIntent = new Intent().setComponent(new ComponentName(context.getPackageName(), GcmIntentService.class.getName()));
                serviceIntent.putExtra("json_payload", fullData);
                serviceIntent.putExtra("android_notif_id", existingId);
                serviceIntent.putExtra("restoring", true);
                context.startService(serviceIntent);
            } while (cursor.moveToNext());
        }
    } catch (Throwable t) {
        OneSignal.Log(OneSignal.LOG_LEVEL.ERROR, "Error restoring notification records! ", t);
    } finally {
        if (cursor != null && !cursor.isClosed())
            cursor.close();
    }
}