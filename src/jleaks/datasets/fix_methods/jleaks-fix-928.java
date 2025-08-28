public void writeLoaderClasses(String loaderJarResourceName) throws IOException 
{
    URL loaderJar = getClass().getClassLoader().getResource(loaderJarResourceName);
    try (JarInputStream inputStream = new JarInputStream(new BufferedInputStream(loaderJar.openStream()))) {
        JarEntry entry;
        while ((entry = inputStream.getNextJarEntry()) != null) {
            if (entry.getName().endsWith(".class")) {
                writeEntry(new JarArchiveEntry(entry), new InputStreamEntryWriter(inputStream, false));
            }
        }
    }
}