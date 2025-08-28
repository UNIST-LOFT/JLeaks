    public static ICC_Profile getInstance(String fileName) throws IOException {
        InputStream is;
        File f = getProfileFile(fileName);
        if (f != null) {
            is = new FileInputStream(f);
        } else {
            is = getStandardProfileInputStream(fileName);
        }
        if (is == null) {
            throw new IOException("Cannot open file " + fileName);
        }
        try (is) {
            return getInstance(is);
        }
    }