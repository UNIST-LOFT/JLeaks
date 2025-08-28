    public static String getDigestOf( InputStream source) throws IOException {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");

            byte[] buffer = new byte[1024];
            try (DigestInputStream in = new DigestInputStream(source, md5)) {
                while (in.read(buffer) >= 0)
                    ; // simply discard the input
            }
            return toHexString(md5.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("MD5 not installed",e);    // impossible
        }
        /* JENKINS-18178: confuses Maven 2 runner
        try {
            return DigestUtils.md5Hex(source);
        } finally {
            source.close();
        }
        */
    }
