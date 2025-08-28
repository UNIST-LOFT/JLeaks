final public void addInputJar(File f) throws IOException 
{
    try (final JarFile jf = new JarFile(f, false)) {
        for (Enumeration<JarEntry> e = jf.entries(); e.hasMoreElements(); ) {
            JarEntry entry = e.nextElement();
            String name = entry.getName();
            inputs.add(new JarInput(f, name));
        }
    }
}