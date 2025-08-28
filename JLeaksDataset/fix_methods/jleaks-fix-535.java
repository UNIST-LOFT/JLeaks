public List<SpoonFile> getFiles() 
{
    // Indexing content
    if (files == null) {
        files = new ArrayList<>();
        final int buffer = 2048;
        try (ZipInputStream zipInput = new ZipInputStream(new BufferedInputStream(new FileInputStream(file)));
            ByteArrayOutputStream output = new ByteArrayOutputStream(buffer)) {
            ZipEntry entry;
            while ((entry = zipInput.getNextEntry()) != null) {
                // deflate in buffer
                int count;
                byte[] data = new byte[buffer];
                while ((count = zipInput.read(data, 0, buffer)) != -1) {
                    output.write(data, 0, count);
                }
                files.add(new ZipFile(this, entry.getName(), output.toByteArray()));
                output.reset();
            }
        } catch (Exception e) {
            Launcher.LOGGER.error(e.getMessage(), e);
        }
    }
    return files;
}