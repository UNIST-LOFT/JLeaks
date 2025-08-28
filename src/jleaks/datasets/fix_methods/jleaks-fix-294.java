
  private static void streamEndpointResponse(final Server server,
      final RoutingContext routingContext,
      final StreamingOutput streamingOutput) {
    final WorkerExecutor workerExecutor = server.getWorkerExecutor();
    final VertxCompletableFuture<Void> vcf = new VertxCompletableFuture<>();
    workerExecutor.executeBlocking(promise -> {
      final OutputStream ros = new ResponseOutputStream(routingContext.response());
      routingContext.request().connection().closeHandler(v -> {
        // Close the OutputStream on close of the HTTP connection
        try {
          ros.close();
        } catch (IOException e) {
          promise.fail(e);
        }
      });
      try {
        streamingOutput.write(new BufferedOutputStream(ros));
        promise.complete();
      } catch (Exception e) {
        promise.fail(e);
      } finally {
        try {
          ros.close();
        } catch (IOException ignore) {
          // Ignore - it might already be closed
        }
      }
    }, vcf);
  }