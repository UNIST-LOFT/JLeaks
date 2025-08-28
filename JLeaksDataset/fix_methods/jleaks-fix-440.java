public static InputStream[] openStream(final String... urls) throws IOException 
{
    if (null == path) {
        return null;
    }
    try {
        final String checkedPath;
        if (path.startsWith("/")) {
            checkedPath = path;
        } else {
            checkedPath = "/" + path;
        }
        return clazz.getResourceAsStream(checkedPath);
    } catch (final Exception e) {
        if (logErrors) {
            LOGGER.error("Failed to create input stream for " + path, e);
            return null;
        } else {
            throw e;
        }
    }
}