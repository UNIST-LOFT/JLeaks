    public void shutdown() throws Exception {
        logger.info("LiteConsumer shutting down");
        super.shutdown();
        httpClient.close();
        started.compareAndSet(true, false);
        logger.info("LiteConsumer shutdown");
    }
