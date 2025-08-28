public static void waitOnSafeMode(final Configuration conf,
final long wait)
throws IOException {
    FileSystem fs = FileSystem.get(conf);
    if (!(fs instanceof DistributedFileSystem))
        return;
    DistributedFileSystem dfs = (DistributedFileSystem) fs;
    // Make sure dfs is not in safe mode
    while (isInSafeMode(dfs)) {
        LOG.info("Waiting for dfs to exit safe mode...");
        try {
            Thread.sleep(wait);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw (InterruptedIOException) new InterruptedIOException().initCause(e);
        }
    }
}