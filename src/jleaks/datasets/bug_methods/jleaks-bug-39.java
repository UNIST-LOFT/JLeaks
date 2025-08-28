  public void close() throws IOException {
    if (mClosed) {
      return;
    }
    flush();
    mCloser.close();
    if (mWrittenBytes > 0) {
      try {
        mBlockWorkerClient.cacheBlock(mBlockId);
      } catch (AlluxioException e) {
        throw new IOException(e);
      } finally {
        releaseAndClose();
      }
      Metrics.BLOCKS_WRITTEN_LOCAL.inc();
    } else {
      releaseAndClose();
    }
  }
