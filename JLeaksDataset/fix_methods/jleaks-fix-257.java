public static String getVersion() {
    try {
        ClassLoader classLoader = Jadx.class.getClassLoader();
        if (classLoader != null) {
            Enumeration<URL> resources = classLoader.getResources("META-INF/MANIFEST.MF");
            while (resources.hasMoreElements()) {
                try (InputStream is = resources.nextElement().openStream()) {
                    Manifest manifest = new Manifest(is);
                    String ver = manifest.getMainAttributes().getValue("jadx-version");
                    if (ver != null) {
                        return ver;
                    }
                }
            }
        }
    } catch (Exception e) {
        LOG.error("Can't get manifest file", e);
    }
    return "dev";
}