public void close() throws IOException 
{
    if (mClosed) {
        return;
    }
    try {
        flush();
        if (mWrittenBytes > 0) {
            try {
                mBlockWorkerClient.cacheBlock(mBlockId);
            } catch (AlluxioException e) {
                throw new IOException(e);
            }
            Metrics.BLOCKS_WRITTEN_LOCAL.inc();
        }
    } finally {
        mClosed = true;
        mCloser.close();
    }
}