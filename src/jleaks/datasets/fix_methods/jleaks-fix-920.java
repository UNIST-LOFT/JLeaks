public void write(OutputStream out) throws IOException 
{
    PrintWriter writer = new PrintWriter(new OutputStreamWriter(out));
    try {
        writer.println(JAR_FILES_KEY);
        for (Iterator<String> i = jarList.iterator(); i.hasNext(); ) {
            String jarFile = i.next();
            writer.println(jarFile);
        }
        writer.println(SRC_DIRS_KEY);
        for (Iterator<String> i = srcDirList.iterator(); i.hasNext(); ) {
            String srcDir = i.next();
            writer.println(srcDir);
        }
        writer.println(AUX_CLASSPATH_ENTRIES_KEY);
        for (Iterator<String> i = auxClasspathEntryList.iterator(); i.hasNext(); ) {
            String auxClasspathEntry = i.next();
            writer.println(auxClasspathEntry);
        }
    } finally {
        writer.close();
    }
    // Project successfully saved
    isModified = false;
}