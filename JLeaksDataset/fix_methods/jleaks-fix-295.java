public static void extractEntry(ZipEntry entry, final InputStream inputStream, File outputDir, boolean overwrite) throws IOException 
{
    final boolean isDirectory = entry.isDirectory();
    final String relativeName = entry.getName();
    final File file = new File(outputDir, relativeName);
    if (file.exists() && !overwrite)
        return;
    FileUtil.createParentDirs(file);
    if (isDirectory) {
        file.mkdir();
    } else {
        try (BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(file));
            BufferedInputStream is = new BufferedInputStream(inputStream)) {
            FileUtil.copy(is, os);
        }
    }
}