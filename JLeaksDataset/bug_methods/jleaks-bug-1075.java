    private static void log(final RqFallback req) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final Throwable error = req.throwable();
        final PrintStream stream = new PrintStream(
            baos, false, StandardCharsets.UTF_8.toString()
        );
        error.printStackTrace(stream);
        stream.close();
        Logger.getLogger(FbLog4j.class).error(
            String.format(
                "%s %s failed with %s: %s",
                new RqMethod.Base(req).method(),
                new RqHref.Base(req).href(),
                req.code(),
                baos.toString("UTF-8")
            )
        );
    }
