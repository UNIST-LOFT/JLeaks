public void run() 
{
    try {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Consumer<KafkaConsumer<byte[], byte[]>> task = tasks.poll(1, TimeUnit.SECONDS);
                if (task != null) {
                    task.accept(consumer);
                }
            } catch (InterruptedException e) {
                throw new KafkaConnectorException(KafkaConnectorErrorCode.CONSUME_THREAD_RUN_ERROR, e);
            }
        }
    } finally {
        try {
            consumer.close();
        } catch (Throwable t) {
            throw new KafkaConnectorException(KafkaConnectorErrorCode.CONSUMER_CLOSE_FAILED, t);
        }
    }
}