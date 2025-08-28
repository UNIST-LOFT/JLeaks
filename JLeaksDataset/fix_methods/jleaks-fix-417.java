public void onLoadComplete(Loader<Cursor> loader, Cursor cursor) 
{
    if (cursor == null) {
        return;
    }
    // If a newer update has happened since we started clean up and
    // return
    synchronized (mLock) {
        if (cursor.isClosed()) {
            Log.wtf(TAG, "Got a closed cursor from onLoadComplete");
            return;
        }
        if (mLastLock != mLock) {
            return;
        }
        final long now = System.currentTimeMillis();
        String tz = Utils.getTimeZone(mContext, mTimezoneChanged);
        // Copy it to a local static cursor.
        MatrixCursor matrixCursor = Utils.matrixCursorFromCursor(cursor);
        try {
            mModel = buildAppWidgetModel(mContext, matrixCursor, tz);
        } finally {
            if (matrixCursor != null) {
                matrixCursor.close();
            }
            if (cursor != null) {
                cursor.close();
            }
        }
        // Schedule an alarm to wake ourselves up for the next update.
        // We also cancel
        // all existing wake-ups because PendingIntents don't match
        // against extras.
        long triggerTime = calculateUpdateTime(mModel, now, tz);
        // If no next-update calculated, or bad trigger time in past,
        // schedule
        // update about six hours from now.
        if (triggerTime < now) {
            Log.w(TAG, "Encountered bad trigger time " + formatDebugTime(triggerTime, now));
            triggerTime = now + UPDATE_TIME_NO_EVENTS;
        }
        final AlarmManager alertManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        final PendingIntent pendingUpdate = CalendarAppWidgetProvider.getUpdateIntent(mContext);
        alertManager.cancel(pendingUpdate);
        alertManager.set(AlarmManager.RTC, triggerTime, pendingUpdate);
        Time time = new Time(Utils.getTimeZone(mContext, null));
        time.setToNow();
        if (time.normalize(true) != sLastUpdateTime) {
            Time time2 = new Time(Utils.getTimeZone(mContext, null));
            time2.set(sLastUpdateTime);
            time2.normalize(true);
            if (time.year != time2.year || time.yearDay != time2.yearDay) {
                final Intent updateIntent = new Intent(Utils.getWidgetUpdateAction(mContext));
                mContext.sendBroadcast(updateIntent);
            }
            sLastUpdateTime = time.toMillis(true);
        }
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(mContext);
        if (mAppWidgetId == -1) {
            int[] ids = widgetManager.getAppWidgetIds(CalendarAppWidgetProvider.getComponentName(mContext));
            widgetManager.notifyAppWidgetViewDataChanged(ids, R.id.events_list);
        } else {
            widgetManager.notifyAppWidgetViewDataChanged(mAppWidgetId, R.id.events_list);
        }
    }
}