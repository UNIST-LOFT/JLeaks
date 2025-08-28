    protected long getMaxItemDateSms() {
        ContentResolver r = getContentResolver();
        String selection = SmsConsts.TYPE + " <> ?";
        String[] selectionArgs = new String[] {
            String.valueOf(SmsConsts.MESSAGE_TYPE_DRAFT)
        };
        String[] projection = new String[] {
            SmsConsts.DATE
        };
        Cursor result = r.query(SMS_PROVIDER, projection, selection, selectionArgs,
                SmsConsts.DATE + " DESC LIMIT 1");

        try
        {
            if (result.moveToFirst()) {
                return result.getLong(0);
            } else {
                return PrefStore.DEFAULT_MAX_SYNCED_DATE;
            }
        }
        catch (RuntimeException e)
        {
            result.close();
            throw e;
        }
    }
