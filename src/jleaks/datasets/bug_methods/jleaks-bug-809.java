    public List<SourceCount> getSources() {
        List<SourceCount> ret = new ArrayList<>();
        if (db != null) {
            Cursor cur = db.rawQuery("select distinct (" + COLUMN_PROVIDER + ") from " + TABLE, null);
            while (cur.moveToNext()) {
                String prov = cur.getString(cur.getColumnIndex(COLUMN_PROVIDER));
                SourceCount c = new SourceCount();
                c.source = prov;
                c.rowCount = getRowCount(prov);
                ret.add(c);
            }
            cur.close();
        }
        return ret;

    }
