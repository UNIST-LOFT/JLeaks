  public void close() throws InterruptedException {
    if (isClosed) return; // it's okay if we over close - same as solrcore
    isClosed = true;
    keeper.close();
    numCloses.incrementAndGet();
  }
