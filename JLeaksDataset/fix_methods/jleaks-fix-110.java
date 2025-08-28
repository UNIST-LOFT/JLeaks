Path replaceWriter(final Path oldPath, final Path newPath, FSHLog.Writer nextWriter,
      final FSDataOutputStream nextHdfsOut)
  throws IOException {
    // Ask the ring buffer writer to pause at a safe point.  Once we do this, the writer
    // thread will eventually pause. An error hereafter needs to release the writer thread
    // regardless -- hence the finally block below.  Note, this method is called from the FSHLog
    // constructor BEFORE the ring buffer is set running so it is null on first time through
    // here; allow for that.
    SyncFuture syncFuture = null;
    SafePointZigZagLatch zigzagLatch = (this.ringBufferEventHandler == null) ? null : this.ringBufferEventHandler.attainSafePoint();
    TraceScope scope = Trace.startSpan("FSHFile.replaceWriter");
    try {
        // Wait on the safe point to be achieved.  Send in a sync in case nothing has hit the
        // ring buffer between the above notification of writer that we want it to go to
        // 'safe point' and then here where we are waiting on it to attain safe point.  Use
        // 'sendSync' instead of 'sync' because we do not want this thread to block waiting on it
        // to come back.  Cleanup this syncFuture down below after we are ready to run again.
        try {
            if (zigzagLatch != null) {
                Trace.addTimelineAnnotation("awaiting safepoint");
                syncFuture = zigzagLatch.waitSafePoint(publishSyncOnRingBuffer());
            }
        } catch (FailedSyncBeforeLogCloseException e) {
            if (isUnflushedEntries())
                throw e;
            // Else, let is pass through to the close.
            LOG.warn("Failed last sync but no outstanding unsync edits so falling through to close; " + e.getMessage());
        }
        // It is at the safe point.  Swap out writer from under the blocked writer thread.
        // TODO: This is close is inline with critical section.  Should happen in background?
        try {
            if (this.writer != null) {
                Trace.addTimelineAnnotation("closing writer");
                this.writer.close();
                Trace.addTimelineAnnotation("writer closed");
            }
            this.closeErrorCount.set(0);
        } catch (IOException ioe) {
            int errors = closeErrorCount.incrementAndGet();
            if (!isUnflushedEntries() && (errors <= this.closeErrorsTolerated)) {
                LOG.warn("Riding over failed WAL close of " + oldPath + ", cause=\"" + ioe.getMessage() + "\", errors=" + errors + "; THIS FILE WAS NOT CLOSED BUT ALL EDITS SYNCED SO SHOULD BE OK");
            } else {
                throw ioe;
            }
        }
        this.writer = nextWriter;
        this.hdfs_out = nextHdfsOut;
        int oldNumEntries = this.numEntries.get();
        this.numEntries.set(0);
        if (oldPath != null) {
            this.byWalRegionSequenceIds.put(oldPath, this.highestRegionSequenceIds);
            this.highestRegionSequenceIds = new HashMap<byte[], Long>();
            long oldFileLen = this.fs.getFileStatus(oldPath).getLen();
            this.totalLogSize.addAndGet(oldFileLen);
            LOG.info("Rolled WAL " + FSUtils.getPath(oldPath) + " with entries=" + oldNumEntries + ", filesize=" + StringUtils.humanReadableInt(oldFileLen) + "; new WAL " + FSUtils.getPath(newPath));
        } else {
            LOG.info("New WAL " + FSUtils.getPath(newPath));
        }
    } catch (InterruptedException ie) {
        // Perpetuate the interrupt
        Thread.currentThread().interrupt();
    } catch (IOException e) {
        long count = getUnflushedEntriesCount();
        LOG.error("Failed close of HLog writer " + oldPath + ", unflushedEntries=" + count, e);
        throw new FailedLogCloseException(oldPath + ", unflushedEntries=" + count, e);
    } finally {
        try {
            // Let the writer thread go regardless, whether error or not.
            if (zigzagLatch != null) {
                zigzagLatch.releaseSafePoint();
                // It will be null if we failed our wait on safe point above.
                if (syncFuture != null)
                    blockOnSync(syncFuture);
            }
        } finally {
            scope.close();
        }
    }
    return newPath;
}