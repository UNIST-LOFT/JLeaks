public void visit(File file, String relativePath) throws IOException 
{
    if (Functions.isWindows())
        relativePath = relativePath.replace('\\', '/');
    if (file.isDirectory())
        relativePath += '/';
    TarArchiveEntry te = new TarArchiveEntry(relativePath);
    int mode = IOUtils.mode(file);
    if (mode != -1)
        te.setMode(mode);
    te.setModTime(file.lastModified());
    long size = 0;
    if (!file.isDirectory()) {
        size = file.length();
        te.setSize(size);
    }
    tar.putArchiveEntry(te);
    try {
        if (!file.isDirectory()) {
            // ensure we don't write more bytes than the declared when we created the entry
            try (FileInputStream fin = new FileInputStream(file);
                BoundedInputStream in = new BoundedInputStream(fin, size)) {
                int len;
                while ((len = in.read(buf)) >= 0) {
                    tar.write(buf, 0, len);
                }
            } catch (IOException e) {
                // log the exception in any case
                IOException ioE = new IOException("Error writing to tar file from: " + file, e);
                throw ioE;
            }
        }
    } finally {
        // always close the entry
        tar.closeArchiveEntry();
    }
    entriesWritten++;
}