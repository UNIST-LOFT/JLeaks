public void close() throws IOException 
{
    try {
        if (mClosed) {
            return;
        }
        updateStreams();
        if (mShouldCachePartiallyReadBlock) {
            readCurrentBlockToEnd();
        }
    } catch (AlluxioStatusException e) {
        throw e.toIOException();
    } finally {
        try {
            if (mCurrentBlockInStream != null) {
                mCurrentBlockInStream.close();
            }
            closeOrCancelCacheStream();
        } catch (AlluxioStatusException e) {
            throw e.toIOException();
        }
        mClosed = true;
    }
}