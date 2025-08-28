public void close() throws InterruptedException 
{
    // it's okay if we over close - same as solrcore
    if (isClosed)
        return;
    isClosed = true;
    try {
        keeper.close();
    } finally {
        connManager.close();
    }
    numCloses.incrementAndGet();
}