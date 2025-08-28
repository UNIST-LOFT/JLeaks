    public void process(OutputStream outputStream, Object element)
            throws IOException {
        IOUtils.copy((InputStream) element, outputStream);
    }
