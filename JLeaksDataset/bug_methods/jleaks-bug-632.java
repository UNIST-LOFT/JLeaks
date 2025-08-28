    public static Document load(File in, @Nullable String charsetName, String baseUri) throws IOException {
        InputStream stream = new FileInputStream(in);
        String name = Normalizer.lowerCase(in.getName());
        if (name.endsWith(".gz") || name.endsWith(".z")) {
            // unfortunately file input streams don't support marks (why not?), so we will close and reopen after read
            boolean zipped = (stream.read() == 0x1f && stream.read() == 0x8b); // gzip magic bytes
            stream.close();
            stream = zipped ? new GZIPInputStream(new FileInputStream(in)) : new FileInputStream(in);
        }
        return parseInputStream(stream, charsetName, baseUri, Parser.htmlParser());
    }
