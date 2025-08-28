@Override
public void start(boolean isLeader) throws IOException {
    Preconditions.checkState(mExecutorService == null);
    mExecutorService = mExecutorServiceFactory.create();
    mIsLeader = isLeader;
    LOG.info("{}: Starting {} master.", getName(), mIsLeader ? "leader" : "standby");
    if (mIsLeader) {
        Preconditions.checkState(mJournal instanceof MutableJournal);
        mJournalWriter = ((MutableJournal) mJournal).getWriter();
        /**
         * The sequence for dealing with the journal before starting as the leader:
         *
         * Phase 1. Recover from a backup checkpoint if the last startup failed while writing the
         * checkpoint.
         *
         * Phase 2. Mark all the logs as completed. Since this master is the leader, it is allowed to
         * write the journal, so it can mark the current log as completed. After this step, the
         * current log will not exist, and all logs will be complete.
         *
         * Phase 3. Reconstruct the state from the journal. This uses the JournalTailer to process all
         * of the checkpoint and the complete logs. Since all logs are complete, after this step,
         * the master will reflect the state of all of the journal entries.
         *
         * Phase 4. Write out the checkpoint. Since this master is completely up-to-date, it
         * writes out the checkpoint. When the checkpoint is closed, it will then delete the
         * complete logs.
         *
         * Since this method is called before the master RPC server starts serving, there is no
         * concurrent access to the master during these phases.
         */
        // Phase 1: Recover from a backup checkpoint if necessary.
        mJournalWriter.recover();
        // Phase 2: Mark all logs as complete, including the current log. After this call, the current
        // log should not exist, and all the logs will be complete.
        mJournalWriter.completeLogs();
        // Phase 3: Replay all the state of the checkpoint and the completed logs.
        JournalTailer catchupTailer;
        if (mStandbyJournalTailer != null && mStandbyJournalTailer.getLatestJournalTailer() != null && mStandbyJournalTailer.getLatestJournalTailer().isValid()) {
            // This master was previously in standby mode, and processed some of the journal. Re-use the
            // same tailer (still valid) to continue processing any remaining journal entries.
            LOG.info("{}: finish processing remaining journal entries (standby -> master).", getName());
            catchupTailer = mStandbyJournalTailer.getLatestJournalTailer();
            catchupTailer.processNextJournalLogs();
        } else {
            // This master has not successfully processed any of the journal, so create a fresh tailer
            // to process the entire journal.
            catchupTailer = JournalTailer.Factory.create(this, mJournal);
            if (catchupTailer.checkpointExists()) {
                LOG.info("{}: process entire journal before becoming leader master.", getName());
                catchupTailer.processJournalCheckpoint(true);
                catchupTailer.processNextJournalLogs();
            } else {
                LOG.info("{}: journal checkpoint does not exist, nothing to process.", getName());
            }
        }
        long latestSequenceNumber = catchupTailer.getLatestSequenceNumber();
        // Phase 4: initialize the journal and write out the checkpoint (the state of all
        // completed logs).
        try (JournalOutputStream checkpointStream = mJournalWriter.getCheckpointOutputStream(latestSequenceNumber)) {
            LOG.info("{}: start writing checkpoint.", getName());
            streamToJournalCheckpoint(checkpointStream);
        }
        LOG.info("{}: done with writing checkpoint.", getName());
        mAsyncJournalWriter = new AsyncJournalWriter(mJournalWriter);
    } else {
        // This master is in standby mode. Start the journal tailer thread. Since the master is in
        // standby mode, its RPC server is NOT serving. Therefore, the only thread modifying the
        // master is this journal tailer thread (no concurrent access).
        mStandbyJournalTailer = new JournalTailerThread(this, mJournal);
        mStandbyJournalTailer.start();
    }
}