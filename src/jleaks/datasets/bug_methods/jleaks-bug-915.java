    public static String stackTrace(final Throwable t) {
        if (t == null) {
            return "null";
        }

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            final PrintStream ps = new PrintStream(out);
            t.printStackTrace(ps);
            ps.flush();
            return new String(out.toByteArray());
        } finally {
            try {
                out.close();
            } catch (final IOException ignored) {
                // ignored
            }
        }
    }
