public static String getVersion() {
    try {
        ClassLoader classLoader = Jadx.class.getClassLoader();
        if (classLoader != null) {
            Enumeration<URL> resources = classLoader.getResources("META-INF/MANIFEST.MF");
            while (resources.hasMoreElements()) {
                Manifest manifest = new Manifest(resources.nextElement().openStream());
                String ver = manifest.getMainAttributes().getValue("jadx-version");
                if (ver != null) {
                    return ver;
                }
            }
        }
    } catch (Exception e) {
        LOG.error("Can't get manifest file", e);
    }
    return "dev";
}