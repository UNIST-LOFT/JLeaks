private List<OsmNotesPoint> checkOsmbugsPoints()
{
    SQLiteDatabase db = getWritableDatabase();
    List<OsmNotesPoint> cachedOsmbugsPoints = new ArrayList<OsmNotesPoint>();
    if (db != null) {
        Cursor query = db.rawQuery(// $NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        "SELECT " + OSMBUGS_COL_ID + ", " + OSMBUGS_COL_TEXT + ", " + OSMBUGS_COL_LAT + "," + OSMBUGS_COL_LON + "," + OSMBUGS_COL_ACTION + "," + OSMBUGS_COL_AUTHOR + " FROM " + OSMBUGS_TABLE_NAME, null);
        if (query.moveToFirst()) {
            do {
                OsmNotesPoint p = new OsmNotesPoint();
                p.setId(query.getLong(0));
                p.setText(query.getString(1));
                p.setLatitude(query.getDouble(2));
                p.setLongitude(query.getDouble(3));
                p.setAction(query.getString(4));
                p.setAuthor(query.getString(5));
                cachedOsmbugsPoints.add(p);
            } while (query.moveToNext());
        }
        query.close();
        db.close();
    }
    return cachedOsmbugsPoints;
}