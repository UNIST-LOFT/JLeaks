  public void render(Context ctx, HealthCheckResults healthCheckResults) throws Exception {
    ByteBuf buffer = byteBufAllocator.buffer();
    boolean first = true;
    boolean unhealthy = false;

    try {
      Writer writer = new OutputStreamWriter(new BufferedOutputStream(new ByteBufOutputStream(buffer)));
      for (Map.Entry<String, HealthCheck.Result> entry : healthCheckResults.getResults().entrySet()) {
        if (first) {
          first = false;
        } else {
          writer.write("\n");
        }
        String name = entry.getKey();
        HealthCheck.Result result = entry.getValue();
        writer.append(name).append(" : ").append(result.isHealthy() ? "HEALTHY" : "UNHEALTHY");
        if (!result.isHealthy()) {
          unhealthy = true;
          writer.append(" [").append(result.getMessage()).append("]");
          if (result.getError() != null) {
            writer.append(" [").append(result.getError().toString()).append("]");
          }
        }
      }
      writer.close();
    } catch (Exception e) {
      buffer.release();
      throw e;
    }

    ctx.getResponse()
      .contentTypeIfNotSet(HttpHeaderConstants.PLAIN_TEXT_UTF8)
      .status(unhealthy ? 503 : 200)
      .send(buffer);
  }
