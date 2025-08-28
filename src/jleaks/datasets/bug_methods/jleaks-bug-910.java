    Optional<CacheFileCopy> findAndCopyCacheFile(String cacheFileName, String cachePath) throws TskCoreException, IngestModuleException  {
        
        Optional<AbstractFile> cacheFileOptional = findCacheFile(cacheFileName, cachePath);
        if (!cacheFileOptional.isPresent()) {
            return Optional.empty(); 
        }
        
        AbstractFile cacheFile = cacheFileOptional.get();
        String tempFilePathname = RAImageIngestModule.getRATempPath(currentCase, moduleName) + cachePath + cacheFile.getName(); //NON-NLS
        try {
            File newFile = new File(tempFilePathname);
            ContentUtils.writeToFile(cacheFile, newFile, context::dataSourceIngestIsCancelled);
            
            RandomAccessFile randomAccessFile = new RandomAccessFile(tempFilePathname, "r");
            FileChannel roChannel = randomAccessFile.getChannel();
            ByteBuffer cacheFileROBuf = roChannel.map(FileChannel.MapMode.READ_ONLY, 0,
                                                        (int) roChannel.size());

            cacheFileROBuf.order(ByteOrder.nativeOrder());
            CacheFileCopy cacheFileCopy = new CacheFileCopy(cacheFile, randomAccessFile, cacheFileROBuf );
            
            if (!cacheFileName.startsWith("f_")) {
                filesTable.put(cachePath + cacheFileName, cacheFileCopy);
            }
            
            return Optional.of(cacheFileCopy);
        }
        catch (ReadContentInputStream.ReadContentInputStreamException ex) {
            String msg = String.format("Error reading Chrome cache file '%s' (id=%d).", //NON-NLS
                    cacheFile.getName(), cacheFile.getId()); 
            throw new IngestModuleException(msg, ex);
        } catch (IOException ex) {
            String msg = String.format("Error writing temp Chrome cache file '%s' (id=%d).", //NON-NLS
                    cacheFile.getName(), cacheFile.getId());
            throw new IngestModuleException(msg, ex);
        }
    }
