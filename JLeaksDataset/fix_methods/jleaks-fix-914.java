public void carve(AbstractFile file, String configFilePath, String outputFolderPath) throws ScalpelException 
{
    if (!initialized) {
        throw new ScalpelException("Scalpel library is not fully initialized. ");
    }
    // basic check of arguments before going to jni land
    if (file == null || configFilePath == null || configFilePath.isEmpty() || outputFolderPath == null || outputFolderPath.isEmpty()) {
        throw new ScalpelException("Invalid arguments for scalpel carving. ");
    }
    final ReadContentInputStream carverInput = new ReadContentInputStream(file);
    try {
        carveNat(carverInput, configFilePath, outputFolderPath);
    } catch (Exception e) {
        logger.log(Level.SEVERE, "Error while caving file " + file, e);
        throw new ScalpelException(e);
    } finally {
        try {
            carverInput.close();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Error closing input stream after carving, file: " + file, ex);
        }
    }
}