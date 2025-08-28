    public boolean contains(@NonNull IBambooStorableItem storableItem) {
        final Class<? extends IBambooStorableItem> classOfStorableItem = storableItem.getClass();
        final Cursor cursor = getAsCursor(
                classOfStorableItem,
                getTypeMetaWithExtra(classOfStorableItem).whereById,
                buildWhereArgsByInternalId(storableItem),
                null
        );

        if (cursor == null || !cursor.moveToFirst()) {
            return false;
        } else {
            cursor.close();
            return true;
        }
    }
