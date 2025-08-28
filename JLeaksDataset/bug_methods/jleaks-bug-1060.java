    private boolean fetch(FileSystem fs,
                          Path source,
                          File dest,
                          AsyncOperationStatus status,
                          HdfsCopyStats stats,
                          String storeName,
                          long pushVersion,
                          Long diskQuotaSizeInKB) throws IOException {

        try {

            FetchStrategy fetchStrategy = new BasicFetchStrategy(this,
                                                                 fs,
                                                                 stats,
                                                                 status,
                                                                 bufferSize);
            if(!fs.isFile(source)) {
                Utils.mkdirs(dest);
                HdfsDirectory directory = new HdfsDirectory(fs, source);

                HdfsFile metadataFile = directory.getMetadataFile();
                Long estimatedDiskSize = -1L;

                if(metadataFile != null) {
                    File copyLocation = new File(dest, metadataFile.getPath().getName());
                    fetchStrategy.fetch(metadataFile, copyLocation, null);
                    directory.initializeMetadata(copyLocation);
                    String diskSizeInBytes = (String) directory.getMetadata()
                                                               .get(ReadOnlyStorageMetadata.DISK_SIZE_IN_BYTES);
                    estimatedDiskSize = (diskSizeInBytes != null && diskSizeInBytes != "") ? (Long.parseLong(diskSizeInBytes))
                                                                                          : -1L;
                }


                /*
                 * Only check quota for those stores:that are listed in the
                 * System store - voldsys$_store_quotas and have non -1 values.
                 * Others are either:
                 * 
                 * 1. already existing non quota-ed store, that will be
                 * converted to quota-ed stores in future. (or) 2. new stores
                 * that do not want to be affected by the disk quota feature at
                 * all. -1 represents no Quota
                 */

                if(diskQuotaSizeInKB != null
                   && diskQuotaSizeInKB != VoldemortConfig.DEFAULT_STORAGE_SPACE_QUOTA_IN_KB) {
                    checkIfQuotaExceeded(diskQuotaSizeInKB, storeName, dest, estimatedDiskSize);
                } else {
                    if(logger.isDebugEnabled()) {
                        logger.debug("store: " + storeName + " is a Non Quota type store.");
                    }
                }
                Map<HdfsFile, byte[]> fileCheckSumMap = fetchStrategy.fetch(directory, dest);
                return directory.validateCheckSum(fileCheckSumMap);

            } else if(allowFetchOfFiles) {
                Utils.mkdirs(dest);
                HdfsFile file = new HdfsFile(fs.getFileStatus(source));
                String fileName = file.getDiskFileName();
                File copyLocation = new File(dest, fileName);
                fetchStrategy.fetch(file, copyLocation, CheckSumType.NONE);
                return true;
            }
            logger.error("Source " + source.toString() + " should be a directory");
            return false;
        } finally {

        }
    }
