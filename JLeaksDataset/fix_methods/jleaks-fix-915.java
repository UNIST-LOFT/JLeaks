public static String stackTrace(final Throwable t) 
{
    if (t == null) {
        return NULL_STRING;
    }
    try (final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final PrintStream ps = new PrintStream(out)) {
        t.printStackTrace(ps);
        ps.flush();
        return new String(out.toByteArray());
    } catch (final IOException e) {
        // ignored
    }
    return NULL_STRING;
}