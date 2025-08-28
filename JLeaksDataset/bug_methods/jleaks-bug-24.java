  public void close() throws IOException {
    try {
      if (mClosed) {
        return;
      }
      updateStreams();
      if (mShouldCachePartiallyReadBlock) {
        readCurrentBlockToEnd();
      }
      if (mCurrentBlockInStream != null) {
        mCurrentBlockInStream.close();
      }
      closeOrCancelCacheStream();
      mClosed = true;
    } catch (AlluxioStatusException e) {
      throw e.toIOException();
    }
  }
