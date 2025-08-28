    public static CheckedInputStream getInputStream(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        InputStream is;
        switch (getStreamMode(file.getName())) {
        case GZIP:
            is = new GZIPInputStream(fis);
            break;
        case SNAPPY:
            is = new SnappyInputStream(fis);
            break;
        case CHECKED:
        default:
            is = new BufferedInputStream(fis);
        }
        return new CheckedInputStream(is, new Adler32());
    }
