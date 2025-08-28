@Override public synchronized void close() {
        closed = true;

        asyncRunner.shutdown();

        try {
            asyncRunner.awaitTermination(EXECUTOR_SHUTDOWN_TIMEOUT, TimeUnit.MILLISECONDS);
        }
        catch (InterruptedException ignore) {
            // No-op.
        }

        for (ClientChannelHolder hld : channels)
            hld.closeChannel();
    }