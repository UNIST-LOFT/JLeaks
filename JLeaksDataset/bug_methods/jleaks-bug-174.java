  private void rollbackInternal() throws IOException {

    boolean success = false;

    if (infoStream.isEnabled("IW")) {
      infoStream.message("IW", "rollback");
    }
    
    try {
      synchronized(this) {
        finishMerges(false);
        stopMerges = true;
      }

      if (infoStream.isEnabled("IW")) {
        infoStream.message("IW", "rollback: done finish merges");
      }

      // Must pre-close these two, in case they increment
      // changeCount so that we can then set it to false
      // before calling closeInternal
      mergePolicy.close();
      mergeScheduler.close();

      bufferedUpdatesStream.clear();
      processEvents(false, true);
      docWriter.close(); // mark it as closed first to prevent subsequent indexing actions/flushes 
      docWriter.abort(this); // don't sync on IW here
      synchronized(this) {

        if (pendingCommit != null) {
          pendingCommit.rollbackCommit(directory);
          deleter.decRef(pendingCommit);
          pendingCommit = null;
          notifyAll();
        }

        // Don't bother saving any changes in our segmentInfos
        readerPool.dropAll(false);

        // Keep the same segmentInfos instance but replace all
        // of its SegmentInfo instances.  This is so the next
        // attempt to commit using this instance of IndexWriter
        // will always write to a new generation ("write
        // once").
        segmentInfos.rollbackSegmentInfos(rollbackSegments);
        if (infoStream.isEnabled("IW") ) {
          infoStream.message("IW", "rollback: infos=" + segString(segmentInfos));
        }
        

        assert testPoint("rollback before checkpoint");

        // Ask deleter to locate unreferenced files & remove
        // them:
        deleter.checkpoint(segmentInfos, false);
        deleter.refresh();

        lastCommitChangeCount = changeCount;
      }

      success = true;
    } catch (OutOfMemoryError oom) {
      handleOOM(oom, "rollbackInternal");
    } finally {
      synchronized(this) {
        if (!success) {
          closing = false;
          notifyAll();
          if (infoStream.isEnabled("IW")) {
            infoStream.message("IW", "hit exception during rollback");
          }
        }
      }
    }

    closeInternal(false, false);
  }
