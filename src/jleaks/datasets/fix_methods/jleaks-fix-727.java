public static boolean insertShortcut(Context context, ShortcutRecord shortcut) 
{
    SQLiteDatabase db = getDatabase(context);
    // Do not add duplicate shortcuts
    try (Cursor cursor = db.query("shortcuts", new String[] { "package", "intent_uri" }, "package = ? AND intent_uri = ?", new String[] { shortcut.packageName, shortcut.intentUri }, null, null, null, null)) {
        if (cursor.moveToFirst()) {
            return false;
        }
    }
    ContentValues values = new ContentValues();
    values.put("name", shortcut.name);
    values.put("package", shortcut.packageName);
    // Legacy field (for shortcuts before Oreo), not used anymore
    values.put("icon", (String) null);
    // Another legacy field (icon is dynamically retrieved)
    values.put("icon_blob", (String) null);
    values.put("intent_uri", shortcut.intentUri);
    db.insert("shortcuts", null, values);
    return true;
}