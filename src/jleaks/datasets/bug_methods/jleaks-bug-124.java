  protected RowLock getRowLockInternal(byte[] row, boolean readLock) throws IOException {
    // create an object to use a a key in the row lock map
    HashedBytes rowKey = new HashedBytes(row);

    RowLockContext rowLockContext = null;
    RowLockImpl result = null;
    TraceScope traceScope = null;

    // If we're tracing start a span to show how long this took.
    if (Trace.isTracing()) {
      traceScope = Trace.startSpan("HRegion.getRowLock");
      traceScope.getSpan().addTimelineAnnotation("Getting a " + (readLock?"readLock":"writeLock"));
    }

    boolean success = false;
    try {
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
        if (traceScope != null) {
          traceScope.getSpan().addTimelineAnnotation("Failed to get row lock");
        }
        result = null;
        String message = "Timed out waiting for lock for row: " + rowKey + " in region "
            + getRegionInfo().getEncodedName();
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
      if (traceScope != null) {
        traceScope.getSpan().addTimelineAnnotation("Interrupted exception getting row lock");
      }
      Thread.currentThread().interrupt();
      throw iie;
    } finally {
      // Clean up the counts just in case this was the thing keeping the context alive.
      if (!success && rowLockContext != null) {
        rowLockContext.cleanUp();
      }
      if (traceScope != null) {
        traceScope.close();
      }
    }
  }
