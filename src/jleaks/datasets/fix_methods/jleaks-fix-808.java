public static RoutingConfiguration.Builder getDefault() 
{
    if (DEFAULT == null) {
        InputStream resourceAsStream = null;
        try {
            resourceAsStream = RoutingConfiguration.class.getResourceAsStream("routing.xml");
            DEFAULT = parseFromInputStream(resourceAsStream);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        } finally {
            if (resourceAsStream != null) {
                try {
                    resourceAsStream.close();
                } catch (IOException ignore) {
                    // 
                }
            }
        }
    }
    return DEFAULT;
}