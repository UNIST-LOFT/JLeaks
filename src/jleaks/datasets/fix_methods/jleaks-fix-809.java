public List<SourceCount> getSources() 
{
    final SQLiteDatabase db = getDb();
    List<SourceCount> ret = new ArrayList<>();
    if (db == null) {
        return ret;
    }
    Cursor cur = null;
    try {
        cur = db.rawQuery("select " + COLUMN_PROVIDER + ",count(*) from " + TABLE + " group by " + COLUMN_PROVIDER, null);
        while (cur.moveToNext()) {
            final String prov = cur.getString(0);
            final long count = cur.getLong(1);
            SourceCount c = new SourceCount();
            c.source = prov;
            c.rowCount = count;
            ret.add(c);
        }
    } catch (Exception e) {
        catchException(e);
    } finally {
        if (cur != null) {
            cur.close();
        }
    }
    return ret;
}