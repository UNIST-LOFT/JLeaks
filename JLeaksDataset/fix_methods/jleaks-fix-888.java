protected void copyFile(File sourceFile, File targetFile) throws IOException 
{
    ensureFolderExists(targetFile);
    final FileOutputStream out = new FileOutputStream(targetFile);
    try {
        byte[] buffer = new byte[1024];
        int len;
        while ((len = in.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }
    } finally {
        out.close();
    }
}