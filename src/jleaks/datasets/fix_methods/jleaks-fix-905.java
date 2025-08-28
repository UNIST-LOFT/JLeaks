public void addFile(String path) throws IOException 
{
    try (FileInputStream fi = new FileInputStream(path);
        BufferedInputStream origin = new BufferedInputStream(fi, BUFFER_SIZE)) {
        ZipEntry entry = new ZipEntry(path.substring(path.lastIndexOf("/") + 1));
        outputZipStream.putNextEntry(entry);
        int count;
        while ((count = origin.read(buffer, 0, BUFFER_SIZE)) != -1) {
            outputZipStream.write(buffer, 0, count);
        }
    }
    Log.d(TAG, "Added: " + path);
}