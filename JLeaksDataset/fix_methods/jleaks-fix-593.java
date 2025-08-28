/*package*/ static @CheckForNull Manifest parsePluginManifest(URL bundledJpi) 
{
    try (URLClassLoader cl = new URLClassLoader(new URL[] { bundledJpi })) {
        InputStream in = null;
        try {
            URL res = cl.findResource(PluginWrapper.MANIFEST_FILENAME);
            if (res != null) {
                in = getBundledJpiManifestStream(res);
                return new Manifest(in);
            }
        } finally {
            Util.closeAndLogFailures(in, LOGGER, PluginWrapper.MANIFEST_FILENAME, bundledJpi.toString());
        }
    } catch (IOException e) {
        LOGGER.log(WARNING, "Failed to parse manifest of " + bundledJpi, e);
    }
    return null;
}