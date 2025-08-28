    public static String readContentAsString(InputStream is, String encoding) {
        String res = null;
        try {
            res = IOUtils.toString(is, encoding);
            is.close();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
        return res;
    }
