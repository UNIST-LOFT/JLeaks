  public void close() {
    logger.debug("Closing client");
    try {
      connection.getChannel().close().get();
    } catch (final InterruptedException | ExecutionException e) {
      logger.warn("Failure while shutting {}", this.getClass().getName(), e);

      // Preserve evidence that the interruption occurred so that code higher up on the call stack can learn of the
      // interruption and respond to it if it wants to.
      Thread.currentThread().interrupt();
    }
  }
