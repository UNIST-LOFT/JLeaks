protected long getMaxItemDateSms() 
{
    Cursor result = getContentResolver().query(SMS_PROVIDER, new String[] { SmsConsts.DATE }, SmsConsts.TYPE + " <> ?", new String[] { String.valueOf(SmsConsts.MESSAGE_TYPE_DRAFT) }, SmsConsts.DATE + " DESC LIMIT 1");
    try {
        return result.moveToFirst() ? result.getLong(0) : PrefStore.DEFAULT_MAX_SYNCED_DATE;
    } finally {
        if (result != null)
            result.close();
    }
}