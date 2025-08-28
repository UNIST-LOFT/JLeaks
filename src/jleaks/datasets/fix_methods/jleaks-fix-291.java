protected Map<String, byte[]> loadClasses() throws IOException 
{
    Map<String, byte[]> classes = new HashMap<>();
    // iterate jar entries
    try (ZipFile zipFile = new ZipFile(getFile())) {
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            // simple verification to ensure non-classes are not loaded
            ZipEntry entry = entries.nextElement();
            if (!isValidClass(entry))
                continue;
            InputStream stream = zipFile.getInputStream(entry);
            // minimally parse for the name
            byte[] in = IOUtils.toByteArray(stream);
            try {
                String name = new ClassReader(in).getClassName();
                classes.put(name, in);
            } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException ex) {
                Logger.error("Invalid class in \"{}\" - \"{}\"", getFile().getName(), entry.getName());
            }
        }
    }
    return classes;
}