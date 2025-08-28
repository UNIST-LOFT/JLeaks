private static void uApks(String Apks, String dApks) 
{
    File dir = new File(dApks);
    // create output directory if it doesn't exist
    if (!dir.exists() && !dir.mkdirs()) {
        throw new IOException("Failed to create directory: " + dir);
    }
    try (FileInputStream fis = new FileInputStream(Apks)) {
        byte[] buffer = new byte[1024];
        try (ZipInputStream zis = new ZipInputStream(fis)) {
            ZipEntry ze = zis.getNextEntry();
            while (ze != null) {
                String fileName = ze.getName();
                File newFile = new File(dApks + File.separator + fileName);
                // create directories for sub directories in zip
                File parent = newFile.getParentFile();
                if (parent != null) {
                    if (!parent.exists() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directories: " + parent);
                    }
                }
                try (FileOutputStream fos = new FileOutputStream(newFile)) {
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                }
                ze = zis.getNextEntry();
            }
        }
    }
}