Optional<CacheFileCopy> findAndCopyCacheFile(String cacheFileName, String cachePath) throws TskCoreException, IngestModuleException  
{
    Optional<AbstractFile> cacheFileOptional = findCacheFile(cacheFileName, cachePath);
    if (!cacheFileOptional.isPresent()) {
        return Optional.empty();
    }
    AbstractFile cacheFile = cacheFileOptional.get();
    RandomAccessFile randomAccessFile = null;
    // NON-NLS
    String tempFilePathname = RAImageIngestModule.getRATempPath(currentCase, moduleName) + cachePath + cacheFile.getName();
    try {
        File newFile = new File(tempFilePathname);
        ContentUtils.writeToFile(cacheFile, newFile, context::dataSourceIngestIsCancelled);
        randomAccessFile = new RandomAccessFile(tempFilePathname, "r");
        FileChannel roChannel = randomAccessFile.getChannel();
        ByteBuffer cacheFileROBuf = roChannel.map(FileChannel.MapMode.READ_ONLY, 0, (int) roChannel.size());
        cacheFileROBuf.order(ByteOrder.nativeOrder());
        CacheFileCopy cacheFileCopy = new CacheFileCopy(cacheFile, randomAccessFile, cacheFileROBuf);
        if (!cacheFileName.startsWith("f_")) {
            filesTable.put(cachePath + cacheFileName, cacheFileCopy);
        }
        return Optional.of(cacheFileCopy);
    } catch (IOException ex) {
        try {
            if (randomAccessFile != null) {
                randomAccessFile.close();
            }
        } catch (IOException ex2) {
            // NON-NLS
            logger.log(Level.SEVERE, "Error while trying to close temp file after exception.", ex2);
        }
        String msg = // NON-NLS
        String.// NON-NLS
        format(// NON-NLS
        "Error reading/copying Chrome cache file '%s' (id=%d).", cacheFile.getName(), cacheFile.getId());
        throw new IngestModuleException(msg, ex);
    }
}