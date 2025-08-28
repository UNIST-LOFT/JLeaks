    protected void saveToFile(File file) throws IOException {
        try (OutputStream stream = new FileOutputStream(file)) {
            // writeValue(OutputStream) is documented to use JsonEncoding.UTF8
            ParsingUtilities.defaultWriter.writeValue(stream, this);
            saveProjectMetadata(getModifiedProjectIds());
        }
    }
