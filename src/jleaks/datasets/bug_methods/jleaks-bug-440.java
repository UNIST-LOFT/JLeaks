    public static InputStream[] openStream(final String... urls) throws IOException {
        final InputStream[] rtn = new InputStream[urls.length];
        int count = 0;
        for (final String url : urls) {
            rtn[count++] = openStream(url);
        }
        return rtn;
    }
