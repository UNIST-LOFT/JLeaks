public void execute() throws IOException 
{
    try (OutputStream os = new FileOutputStream(jarFile);
        BufferedOutputStream bos = new BufferedOutputStream(os);
        JarOutputStream out = new JarOutputStream(bos)) {
        // Create the manifest entry in the Jar file
        writeManifestEntry(out, manifestContent());
        for (Map.Entry<String, Path> entry : jarEntries.entrySet()) {
            copyEntry(out, entry.getKey(), entry.getValue());
        }
    }
}