    private <E> List<E> loadFromDB(SQLiteOpenHelper helper,
            PersistableResource<E> persistableResource) {
        Cursor cursor = persistableResource.getCursor(helper
                .getReadableDatabase());
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
    }
