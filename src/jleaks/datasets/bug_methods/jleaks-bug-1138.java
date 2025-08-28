public void close() throws IOException {
  if (client != null) {
    client.close();
  }
  if (refreshExecutor != null) {
    ScheduledExecutorService service = refreshExecutor;
    this.refreshExecutor = null;

    try {
      if (service.awaitTermination(1, TimeUnit.MINUTES)) {
        LOG.warn("Timed out waiting for refresh executor to terminate");
      }
    } catch (InterruptedException e) {
      LOG.warn("Interrupted while waiting for refresh executor to terminate", e);
      Thread.currentThread().interrupt();
    }
  }
}