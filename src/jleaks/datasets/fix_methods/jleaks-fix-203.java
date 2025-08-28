public static boolean classExistsInJar(java.io.File jar, String fqcn) 
{
    java.net.URLClassLoader loader = null;
    try {
        loader = (URLClassLoader) ClassLoaderUtils.loadJar(jar);
        Class.forName(fqcn, false, loader);
        return true;
    } catch (ClassNotFoundException | NoClassDefFoundError | IOException e) {
        return false;
    } finally {
        if (loader != null) {
            try {
                loader.close();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }
}