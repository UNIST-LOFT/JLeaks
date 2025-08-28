public synchronized void shutdown() 
{
    try {
        if (live) {
            stop();
        }
    } finally {
        es.shutdownNow();
        if (serverSocketChannel != null) {
            try {
                logger.log(Level.FINEST, "Closing server socket channel: " + serverSocketChannel);
                serverSocketChannel.close();
            } catch (IOException ignore) {
                logger.log(Level.FINEST, ignore.getMessage(), ignore);
            }
        }
    }
}