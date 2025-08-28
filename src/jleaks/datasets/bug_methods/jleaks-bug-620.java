    public static ICC_Profile getInstance(String fileName) throws IOException {
        ICC_Profile thisProfile;
        InputStream is = null;


        File f = getProfileFile(fileName);
        if (f != null) {
            is = new FileInputStream(f);
        } else {
            is = getStandardProfileInputStream(fileName);
        }
        if (is == null) {
            throw new IOException("Cannot open file " + fileName);
        }

        thisProfile = getInstance(is);

        is.close();    /* close the file */

        return thisProfile;
    }
