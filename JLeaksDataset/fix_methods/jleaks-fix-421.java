public static String writeClasspathToFile(String classpath) 
{
    try {
        File file = File.createTempFile("EvoSuite_classpathFile", ".txt");
        file.deleteOnExit();
        try (BufferedWriter out = new BufferedWriter(new FileWriter(file))) {
            out.write(classpath);
            out.newLine();
        }
        return file.getAbsolutePath();
    } catch (Exception e) {
        throw new IllegalStateException("Failed to create tmp file for classpath specification: " + e.getMessage());
    }
}