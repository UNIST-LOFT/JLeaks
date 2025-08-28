public static void extract(InputStream is, File outputFolder) throws IOException 
{
    ZipInputStream zis = new ZipInputStream(is);
    ZipEntry entry;
    byte[] buffer = new byte[1024];
    while ((entry = zis.getNextEntry()) != null) {
        File outputFile = new File(outputFolder.getCanonicalPath() + File.separatorChar + entry.getName());
        File outputParent = new File(outputFile.getParent());
        outputParent.mkdirs();
        if (entry.isDirectory()) {
            if (!outputFile.exists()) {
                outputFile.mkdir();
            }
        } else {
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
            }
        }
    }
}