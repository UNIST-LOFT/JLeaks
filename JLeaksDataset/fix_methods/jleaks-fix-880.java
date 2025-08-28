    public void process(OutputStream outputStream, Object element)
            throws IOException {
        try (InputStream is = (InputStream) element) {
            IOUtils.copy(is, outputStream);
        }
    }
