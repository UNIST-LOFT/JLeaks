  private static void streamEndpointResponse(final Server server, final HttpServerResponse response,
      final StreamingOutput streamingOutput) {
    final WorkerExecutor workerExecutor = server.getWorkerExecutor();
    final VertxCompletableFuture<Void> vcf = new VertxCompletableFuture<>();
    workerExecutor.executeBlocking(promise -> {
      try (OutputStream os = new BufferedOutputStream(new ResponseOutputStream(response))) {
        streamingOutput.write(os);
      } catch (Exception e) {
        promise.fail(e);
      }
    }, vcf);
  }
