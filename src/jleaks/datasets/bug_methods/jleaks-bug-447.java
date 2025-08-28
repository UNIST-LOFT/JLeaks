    public static byte[] read(File file) {
        byte[] bytes = {};
        try {
            bytes = new byte[(int) file.length()];
            InputStream is = new FileInputStream(file);
            is.read(bytes);
            is.close();
        } catch (IOException e) {
            Timber.e(e);
        }
        return bytes;
    }
