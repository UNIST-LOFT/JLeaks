    private void moveFile(File fromFile, File toFile) throws IOException {

        boolean rename = fromFile.renameTo(toFile);
        if (!rename) {
            Log.i("moveFile", "rename");
            try {
                InputStream ist = new FileInputStream(fromFile);
                OutputStream ost = new FileOutputStream(toFile);
                copyFile(ist, ost);
                if (!fromFile.delete()) {
                    throw new IOException("Failed to delete original file '" + fromFile + "'");
                }
            } catch (FileNotFoundException e) {
                throw e;
            }
        }
    }
