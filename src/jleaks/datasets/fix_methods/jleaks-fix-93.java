    public void shutdown() throws Exception {
        logger.info("LiteConsumer shutting down");
        super.shutdown();
        if (consumeExecutor != null) {
            consumeExecutor.shutdown();
        }
        scheduler.shutdown();
        started.compareAndSet(true, false);
        logger.info("LiteConsumer shutdown");
    }
