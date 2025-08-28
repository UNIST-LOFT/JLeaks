public void write(String text) throws IOException 
{
    file.getParentFile().mkdirs();
    AtomicFileWriter w = new AtomicFileWriter(file);
    try {
        w.write(text);
        w.commit();
    } finally {
        try {
            w.abort();
        } catch (IOException ioe) {
            // swallow exception
        }
        try {
            w.close();
        } catch (IOException ioe) {
            // swallow exception
        }
    }
}