public static void cpdir(File src, File dest) throws BrutException 
{
    dest.mkdirs();
    File[] files = src.listFiles();
    if (files == null) {
        return;
    }
    for (File file : files) {
        File destFile = new File(dest.getPath() + File.separatorChar + file.getName());
        if (file.isDirectory()) {
            cpdir(file, destFile);
            continue;
        }
        try {
            try (InputStream in = new FileInputStream(file)) {
                try (OutputStream out = new FileOutputStream(destFile)) {
                    IOUtils.copy(in, out);
                }
            }
        } catch (IOException ex) {
            throw new BrutException("Could not copy file: " + file, ex);
        }
    }
}