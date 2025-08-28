public void onReset() 
{
    Log.w(TAG, "onReset()");
    try {
        dbSemaphore.acquire(DB_PERMITS);
        db.close();
        db = new ContactsDatabase(context);
    } catch (InterruptedException ie) {
        throw new AssertionError(ie);
    } finally {
        dbSemaphore.release(DB_PERMITS);
    }
    super.onReset();
}