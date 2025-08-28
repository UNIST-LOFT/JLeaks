private File extract(JarFile jar, JarEntry entry) throws IOException 
{
    // expand to temp dir and add to list
    File tempFile = File.createTempFile("migrator.tmp", null);
    // read from jar and write to the tempJar file
    BufferedInputStream inStream = new BufferedInputStream(jar.getInputStream(entry));
    BufferedOutputStream outStream = new BufferedOutputStream(new FileOutputStream(tempFile));
    try {
        int status;
        while ((status = inStream.read()) != -1) {
            outStream.write(status);
        }
    } finally {
        outStream.close();
        inStream.close();
    }
    return tempFile;
}