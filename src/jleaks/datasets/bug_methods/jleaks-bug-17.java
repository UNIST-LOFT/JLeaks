  private void addLock(RWLockResource lock, LockMode mode) {
    if (!endsInWriteLock() && mode == LockMode.WRITE) {
      mFirstWriteLockIndex = mLocks.size();
    }
    mLocks.add(lock);
  }
