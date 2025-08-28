    public synchronized void shutdown() {
        if (!live) return;
        live = false;
        stop();
        if (serverSocketChannel != null) {
            try {
                serverSocketChannel.close();
            } catch (IOException ignore) {
                logger.log(Level.FINEST, ignore.getMessage(), ignore);
            }
        }
        es.shutdownNow();
    }
