private static void log(final RqFallback req) throws IOException 
{
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    final Throwable error = req.throwable();
    final PrintStream stream = new PrintStream(baos, false, StandardCharsets.UTF_8.toString());
    try {
        error.printStackTrace(stream);
    } finally {
        stream.close();
    }
    FbSlf4j.LOGGER.error("{} {} failed with {}: {}", new RqMethod.Base(req).method(), new RqHref.Base(req).href(), req.code(), baos.toString("UTF-8"));
}