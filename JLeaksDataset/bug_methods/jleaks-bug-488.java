    public synchronized void shutdown() {
        if (!live) return;
        live = false;
        stop();
        if (serverSocketChannel != null) {
            try {
                log(Level.FINEST, "Closing server socket channel: " + serverSocketChannel);
                serverSocketChannel.close();
            } catch (IOException ignore) {
                logger.log(Level.FINEST, ignore.getMessage(), ignore);
            }
        }
        es.shutdownNow();
    }
