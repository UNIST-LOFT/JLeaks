public void processJournalCheckpoint(boolean applyToMaster) throws IOException 
{
    // Load the checkpoint.
    LOG.info("{}: Loading checkpoint.", mMaster.getName());
    // The checkpoint stream must be retrieved before retrieving any log streams, because the
    // journal reader verifies that the checkpoint was read before the log streams.
    try (JournalInputStream is = mReader.getCheckpointInputStream()) {
        if (applyToMaster) {
            // Only apply the checkpoint to the master, if specified.
            mMaster.processJournalCheckpoint(is);
        }
        // update the latest sequence number seen.
        mLatestSequenceNumber = is.getLatestSequenceNumber();
    }
}