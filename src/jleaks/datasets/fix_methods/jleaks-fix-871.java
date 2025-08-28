public void render(Context ctx, HealthCheckResults healthCheckResults) throws Exception 
{
    ByteBuf buffer = byteBufAllocator.buffer();
    boolean first = true;
    boolean unhealthy = false;
    try {
        try (Writer writer = new OutputStreamWriter(new BufferedOutputStream(new ByteBufOutputStream(buffer)))) {
            for (Map.Entry<String, HealthCheck.Result> entry : healthCheckResults.getResults().entrySet()) {
                if (first) {
                    first = false;
                } else {
                    writer.write("\n");
                }
                String name = entry.getKey();
                HealthCheck.Result result = entry.getValue();
                unhealthy = unhealthy || !result.isHealthy();
                writer.append(name).append(" : ").append(result.isHealthy() ? "HEALTHY" : "UNHEALTHY");
                String message = result.getMessage();
                if (message != null) {
                    writer.append(" [").append(message).append("]");
                }
                Throwable error = result.getError();
                if (error != null) {
                    writer.append(" [").append(error.toString()).append("]");
                }
            }
        }
    } catch (Exception e) {
        buffer.release();
        throw e;
    }
    ctx.getResponse().contentTypeIfNotSet(HttpHeaderConstants.PLAIN_TEXT_UTF8).status(unhealthy ? 503 : 200).send(buffer);
}