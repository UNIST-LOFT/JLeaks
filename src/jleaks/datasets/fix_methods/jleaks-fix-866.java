public TableReader get(CharSequence name) 
{
    Entry e = getEntry(name);
    long lockOwner = e.lockOwner;
    long thread = Thread.currentThread().getId();
    if (lockOwner != UNLOCKED) {
        LOG.info().$('\'').utf8(name).$("' is locked [owner=").$(lockOwner).$(']').$();
        throw EntryLockedException.instance("unknown");
    }
    do {
        for (int i = 0; i < ENTRY_SIZE; i++) {
            if (Unsafe.cas(e.allocations, i, UNALLOCATED, thread)) {
                // got lock, allocate if needed
                R r = e.readers[i];
                if (r == null) {
                    try {
                        LOG.info().$("open '").utf8(name).$("' [at=").$(e.index).$(':').$(i).$(']').$();
                        r = new R(this, e, i, name);
                    } catch (CairoException ex) {
                        Unsafe.arrayPutOrdered(e.allocations, i, UNALLOCATED);
                        throw ex;
                    }
                    e.readers[i] = r;
                    notifyListener(thread, name, PoolListener.EV_CREATE, e.index, i);
                } else {
                    try {
                        r.goActive();
                    } catch (Throwable ex) {
                        r.close();
                        throw ex;
                    }
                    notifyListener(thread, name, PoolListener.EV_GET, e.index, i);
                }
                if (isClosed()) {
                    e.readers[i] = null;
                    r.goodbye();
                    LOG.info().$('\'').utf8(name).$("' born free").$();
                    return r;
                }
                LOG.debug().$('\'').utf8(name).$("' is assigned [at=").$(e.index).$(':').$(i).$(", thread=").$(thread).$(']').$();
                return r;
            }
        }
        LOG.debug().$("Thread ").$(thread).$(" is moving to entry ").$(e.index + 1).$();
        // all allocated, create next entry if possible
        if (Unsafe.getUnsafe().compareAndSwapInt(e, NEXT_STATUS, NEXT_OPEN, NEXT_ALLOCATED)) {
            LOG.debug().$("Thread ").$(thread).$(" allocated entry ").$(e.index + 1).$();
            e.next = new Entry(e.index + 1, clock.getTicks());
        }
        e = e.next;
    } while (e != null && e.index < maxSegments);
    // max entries exceeded
    notifyListener(thread, name, PoolListener.EV_FULL, -1, -1);
    LOG.info().$("could not get, busy [table=`").utf8(name).$("`, thread=").$(thread).$(", retries=").$(this.maxSegments).$(']').$();
    throw EntryUnavailableException.instance("unknown");
}