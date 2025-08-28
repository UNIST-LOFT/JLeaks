private void addLock(RWLockResource lock, LockMode mode) 
{
    if (!endsInWriteLock() && mode == LockMode.WRITE) {
        mFirstWriteLockIndex = mLocks.size();
    }
    try {
        mLocks.add(lock);
    } catch (Error e) {
        // If adding to mLocks fails due to OOM, this lock
        // will not be tracked so we must close it manually
        lock.close();
        throw e;
    }
}