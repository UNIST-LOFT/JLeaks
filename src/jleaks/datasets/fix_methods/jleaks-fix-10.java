private void moveFile(File fromFile, File toFile) throws IOException 
{
    boolean rename = fromFile.renameTo(toFile);
    if (!rename) {
        Log.i("moveFile", "rename");
        InputStream ist = null;
        OutputStream ost = null;
        try {
            ist = new FileInputStream(fromFile);
            ost = new FileOutputStream(toFile);
            copyFile(ist, ost);
            if (!fromFile.delete()) {
                throw new IOException("Failed to delete original file '" + fromFile + "'");
            }
        } catch (FileNotFoundException e) {
            throw e;
        } finally {
            if (ist != null) {
                ist.close();
            }
            if (ost != null) {
                ost.close();
            }
        }
    }
}