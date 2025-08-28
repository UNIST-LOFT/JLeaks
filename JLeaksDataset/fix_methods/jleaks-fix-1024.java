private void addToZip(final File file, final ZipOutputStream zos) throws IOException 
{
    final FileInputStream is;
    try {
        is = new FileInputStream(file);
    } catch (FileNotFoundException e) {
        LOG.warning("File " + file.getAbsolutePath() + " not found. It can not be saved in this version.");
        return;
    }
    try {
        final ZipEntry entry = new ZipEntry(file.getName());
        zos.putNextEntry(entry);
        final int size = (int) file.length();
        final byte[] bytes = new byte[size];
        is.read(bytes);
        zos.write(bytes, 0, size);
    } finally {
        is.close();
    }
}