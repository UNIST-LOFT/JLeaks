private static Response failure(final Throwable err, final int code){
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    final PrintStream stream = new PrintStream(baos, false, StandardCharsets.UTF_8.toString());
    try {
        err.printStackTrace(stream);
    } finally {
        stream.close();
    }
    return new RsWithStatus(new RsText(new ByteArrayInputStream(baos.toByteArray())), code);
}