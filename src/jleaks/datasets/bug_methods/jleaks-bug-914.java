    public void carve(AbstractFile file, String configFilePath, String outputFolderPath) throws ScalpelException {
        if (!initialized) {
            throw new ScalpelException("Scalpel library is not fully initialized. ");
        }

        //basic check of arguments before going to jni land
        if (file == null || configFilePath == null || configFilePath.isEmpty()
                || outputFolderPath == null || outputFolderPath.isEmpty()) {
            throw new ScalpelException("Invalid arguments for scalpel carving. ");
        }

        final ReadContentInputStream carverInput = new ReadContentInputStream(file);

        carveNat(carverInput, configFilePath, outputFolderPath);
    }
