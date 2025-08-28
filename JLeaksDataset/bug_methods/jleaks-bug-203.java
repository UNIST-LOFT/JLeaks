    public static boolean classExistsInJar(java.io.File jar, String fqcn) {
        try {
            java.net.URLClassLoader loader = (URLClassLoader) ClassLoaderUtils.loadJar(jar);
            Class.forName(fqcn, false, loader);
            loader.close();
            return true;
        } catch (ClassNotFoundException | NoClassDefFoundError | IOException e) {
            return false;
        }
    }
