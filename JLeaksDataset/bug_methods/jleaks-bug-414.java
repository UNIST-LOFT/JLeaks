    public void releaseLock(FileLock lock) throws IOException {
        String lockPath = LOCK_MAP.remove(lock);
        if (lockPath == null) { throw new LockException("Cannot release unobtained lock"); }
        lock.release();
        Boolean removed = LOCK_HELD.remove(lockPath);
        if (removed == false) { throw new LockException("Lock path was not marked as held: " + lockPath); }
    }
