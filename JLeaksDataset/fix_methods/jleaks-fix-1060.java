private boolean fetch(FileSystem fs,
Path source,
File dest,
AsyncOperationStatus status,
HdfsCopyStats stats,
String storeName,
long pushVersion,
Long diskQuotaSizeInKB) throws IOException {
    AdminClient adminClient = null;
    try {
        adminClient = new AdminClient(metadataStore.getCluster(), new AdminClientConfig(), new ClientConfig());
        Versioned<String> diskQuotaSize = adminClient.quotaMgmtOps.getQuotaForNode(storeName, QuotaType.STORAGE_SPACE, metadataStore.getNodeId());
        Long diskQuoataSizeInKB = (diskQuotaSize == null) ? null : (Long.parseLong(diskQuotaSize.getValue()));
        logger.info("Starting fetch for : " + sourceFileUrl);
        return fetchFromSource(sourceFileUrl, destinationFile, status, storeName, pushVersion, diskQuoataSizeInKB);
    } finally {
        if (adminClient != null) {
            IOUtils.closeQuietly(adminClient);
        }
    }
}