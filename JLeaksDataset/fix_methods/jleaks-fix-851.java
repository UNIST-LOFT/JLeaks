private <E> List<E> loadFromDB(SQLiteOpenHelper helper,
PersistableResource<E> persistableResource) {
    SQLiteDatabase db = helper.getReadableDatabase();
    try {
        Cursor cursor = persistableResource.getCursor(db);
        try {
            if (!cursor.moveToFirst())
                return null;
            List<E> cached = new ArrayList<E>();
            do {
                cached.add(persistableResource.loadFrom(cursor));
            } while (cursor.moveToNext());
            return cached;
        } finally {
            cursor.close();
        }
    } finally {
        db.close();
    }
}