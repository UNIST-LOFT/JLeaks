private void readObject(ObjectInputStream in){
    // read values
    in.defaultReadObject();
    // PAYARA-953 protect against null byte attacks
    if (repository != null) {
        if (repository.getPath().contains("\0")) {
            throw new IOException("Repository path cannot contain a null byte");
        }
        if (!repository.isDirectory()) {
            throw new IOException("Repository path " + repository.getPath() + " is not a directory ");
        }
    }
    if (dfosFile != null) {
        if (dfosFile.getPath().contains("\0")) {
            throw new IOException("File path cannot contain a null byte");
        }
    }
    // END PAYARA-953
    try (OutputStream output = getOutputStream()) {
        if (cachedContent != null) {
            output.write(cachedContent);
        } else {
            FileInputStream input = new FileInputStream(dfosFile);
            Streams.copy(input, output, false);
            deleteFile(dfosFile);
            dfosFile = null;
        }
    }
    cachedContent = null;
}