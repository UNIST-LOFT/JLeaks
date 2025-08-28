protected RowLock getRowLockInternal(byte[] row, boolean readLock) throws IOException 
{
    // create an object to use a a key in the row lock map
    HashedBytes rowKey = new HashedBytes(row);
    RowLockContext rowLockContext = null;
    RowLockImpl result = null;
    boolean success = false;
    try (TraceScope scope = TraceUtil.createTrace("HRegion.getRowLock")) {
        TraceUtil.addTimelineAnnotation("Getting a " + (readLock ? "readLock" : "writeLock"));
        // Keep trying until we have a lock or error out.
        // TODO: do we need to add a time component here?
        while (result == null) {
            rowLockContext = computeIfAbsent(lockedRows, rowKey, () -> new RowLockContext(rowKey));
            // Now try an get the lock.
            // This can fail as
            if (readLock) {
                result = rowLockContext.newReadLock();
            } else {
                result = rowLockContext.newWriteLock();
            }
        }
        int timeout = rowLockWaitDuration;
        boolean reachDeadlineFirst = false;
        Optional<RpcCall> call = RpcServer.getCurrentCall();
        if (call.isPresent()) {
            long deadline = call.get().getDeadline();
            if (deadline < Long.MAX_VALUE) {
                int timeToDeadline = (int) (deadline - System.currentTimeMillis());
                if (timeToDeadline <= this.rowLockWaitDuration) {
                    reachDeadlineFirst = true;
                    timeout = timeToDeadline;
                }
            }
        }
        if (timeout <= 0 || !result.getLock().tryLock(timeout, TimeUnit.MILLISECONDS)) {
            TraceUtil.addTimelineAnnotation("Failed to get row lock");
            result = null;
            String message = "Timed out waiting for lock for row: " + rowKey + " in region " + getRegionInfo().getEncodedName();
            if (reachDeadlineFirst) {
                throw new TimeoutIOException(message);
            } else {
                // If timeToDeadline is larger than rowLockWaitDuration, we can not drop the request.
                throw new IOException(message);
            }
        }
        rowLockContext.setThreadName(Thread.currentThread().getName());
        success = true;
        return result;
    } catch (InterruptedException ie) {
        LOG.warn("Thread interrupted waiting for lock on row: " + rowKey);
        InterruptedIOException iie = new InterruptedIOException();
        iie.initCause(ie);
        TraceUtil.addTimelineAnnotation("Interrupted exception getting row lock");
        Thread.currentThread().interrupt();
        throw iie;
    } finally {
        // Clean up the counts just in case this was the thing keeping the context alive.
        if (!success && rowLockContext != null) {
            rowLockContext.cleanUp();
        }
    }
}