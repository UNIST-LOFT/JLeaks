public void closeWriter(IndexWriter writer) throws IOException 
{
    boolean clearRequestInfo = false;
    solrCoreState.getCommitLock().lock();
    try {
        SolrQueryRequest req = new LocalSolrQueryRequest(core, new ModifiableSolrParams());
        SolrQueryResponse rsp = new SolrQueryResponse();
        if (SolrRequestInfo.getRequestInfo() == null) {
            clearRequestInfo = true;
            // important for debugging
            SolrRequestInfo.setRequestInfo(new SolrRequestInfo(req, rsp));
        }
        if (!commitOnClose) {
            if (writer != null) {
                writer.rollback();
            }
            // we shouldn't close the transaction logs either, but leaving them open
            // means we can't delete them on windows (needed for tests)
            if (ulog != null)
                ulog.close(false);
            return;
        }
        // do a commit before we quit?
        boolean tryToCommit = writer != null && ulog != null && ulog.hasUncommittedChanges() && ulog.getState() == UpdateLog.State.ACTIVE;
        try {
            if (tryToCommit) {
                log.info("Committing on IndexWriter close.");
                CommitUpdateCommand cmd = new CommitUpdateCommand(req, false);
                cmd.openSearcher = false;
                cmd.waitSearcher = false;
                cmd.softCommit = false;
                // TODO: keep other commit callbacks from being called?
                // this.commit(cmd);        // too many test failures using this method... is it because of callbacks?
                synchronized (solrCoreState.getUpdateLock()) {
                    ulog.preCommit(cmd);
                }
                // todo: refactor this shared code (or figure out why a real CommitUpdateCommand can't be used)
                final Map<String, String> commitData = new HashMap<>();
                commitData.put(SolrIndexWriter.COMMIT_TIME_MSEC_KEY, String.valueOf(System.currentTimeMillis()));
                writer.setCommitData(commitData);
                writer.commit();
                synchronized (solrCoreState.getUpdateLock()) {
                    ulog.postCommit(cmd);
                }
            }
        } catch (Throwable th) {
            log.error("Error in final commit", th);
            if (th instanceof OutOfMemoryError) {
                throw (OutOfMemoryError) th;
            }
        }
        // we went through the normal process to commit, so we don't have to artificially
        // cap any ulog files.
        try {
            if (ulog != null)
                ulog.close(false);
        } catch (Throwable th) {
            log.error("Error closing log files", th);
            if (th instanceof OutOfMemoryError) {
                throw (OutOfMemoryError) th;
            }
        }
        if (writer != null) {
            try {
                writer.waitForMerges();
            } finally {
                writer.close();
            }
        }
    } finally {
        solrCoreState.getCommitLock().unlock();
        if (clearRequestInfo)
            SolrRequestInfo.clearRequestInfo();
    }
}