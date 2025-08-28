
    private static Configuration decodeObject(InputStream in) throws IOException {
        final Object ret;
        try (XMLDecoder d = new XMLDecoder(new BufferedInputStream(in))) {
            ret = d.readObject();
        }

        if (!(ret instanceof Configuration)) {
            throw new IOException("Not a valid config file");
        }
        return (Configuration) ret;
    }
