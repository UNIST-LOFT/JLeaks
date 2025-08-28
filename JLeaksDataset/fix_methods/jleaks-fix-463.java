static Template parsedTemplateForResource(String resourceName) 
{
    InputStream in = TemplateVars.class.getResourceAsStream(resourceName);
    if (in == null) {
        throw new IllegalArgumentException("Could not find resource: " + resourceName);
    }
    try {
        return templateFromInputStream(in);
    } catch (UnsupportedEncodingException e) {
        throw new AssertionError(e);
    } catch (IOException e) {
        try {
            return parsedTemplateFromUrl(resourceName);
        } catch (Throwable t) {
            // Chain the original exception so we can see both problems.
            Throwable cause;
            for (cause = e; cause.getCause() != null; cause = cause.getCause()) {
            }
            cause.initCause(t);
            throw new AssertionError(e);
        }
    } finally {
        try {
            in.close();
        } catch (IOException ignored) {
            // We probably already got an IOException which we're propagating.
        }
    }
}