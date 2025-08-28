public boolean contains(@NonNull IBambooStorableItem storableItem) 
{
    final Class<? extends IBambooStorableItem> classOfStorableItem = storableItem.getClass();
    final Cursor cursor = getAsCursor(classOfStorableItem, getTypeMetaWithExtra(classOfStorableItem).whereById, buildWhereArgsByInternalId(storableItem), null);
    try {
        return cursor != null && cursor.moveToFirst();
    } finally {
        if (cursor != null) {
            cursor.close();
        }
    }
}