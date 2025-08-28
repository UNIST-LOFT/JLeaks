public synchronized void shutdown() {
        executor.shutdown();

        try {
            writeAheadLog.shutdown();
        } catch (final IOException ioe) {
            logger.warn("Failed to shut down {} successfully due to {}", this, ioe.toString());
            logger.warn("", ioe);
        }
    }