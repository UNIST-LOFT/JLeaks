private void runCompression(File oldFile) 
{
    File gzippedFile = new File(oldFile.getPath() + ".gz");
    try (GZIPOutputStream compressor = new GZIPOutputStream(new FileOutputStream(gzippedFile), 0x100000);
        FileInputStream inputStream = new FileInputStream(oldFile)) {
        byte[] buffer = new byte[0x100000];
        for (int read = inputStream.read(buffer); read > 0; read = inputStream.read(buffer)) {
            compressor.write(buffer, 0, read);
        }
        compressor.finish();
        compressor.flush();
        NativeIO nativeIO = new NativeIO();
        // Drop from cache in case somebody else has a reference to it preventing from dying quickly.
        nativeIO.dropFileFromCache(oldFile);
        oldFile.delete();
        nativeIO.dropFileFromCache(gzippedFile);
    } catch (IOException e) {
        logger.warning("Got '" + e + "' while compressing '" + oldFile.getPath() + "'.");
    }
}