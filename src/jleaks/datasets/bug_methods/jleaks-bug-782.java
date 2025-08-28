    protected boolean saveToFile(File file) throws IOException {
        OutputStream stream = new FileOutputStream(file);
        List<Long> modified = getModifiedProjectIds();
        boolean saveWasNeeded = (modified.size() > 0) || (_preferenceStore.isDirty());
        try {
            // writeValue(OutputStream) is documented to use JsonEncoding.UTF8
            ParsingUtilities.defaultWriter.writeValue(stream, this);
            saveProjectMetadata(modified);
        } finally {
            stream.close();
        }
        return saveWasNeeded;
    }
