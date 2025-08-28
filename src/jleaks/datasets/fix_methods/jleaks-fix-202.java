public static ReadHandle open(ScheduledExecutorService executor,
BlobStore blobStore, String bucket, String key, String indexKey,
VersionCheck versionCheck,
long ledgerId, int readBufferSize)
throws IOException {
    Blob blob = blobStore.getBlob(bucket, indexKey);
    versionCheck.check(indexKey, blob);
    OffloadIndexBlockBuilder indexBuilder = OffloadIndexBlockBuilder.create();
    OffloadIndexBlock index;
    try (InputStream payLoadStream = blob.getPayload().openStream()) {
        index = (OffloadIndexBlock) indexBuilder.fromStream(payLoadStream);
    }
    BackedInputStream inputStream = new BlobStoreBackedInputStreamImpl(blobStore, bucket, key, versionCheck, index.getDataObjectLength(), readBufferSize);
    return new BlobStoreBackedReadHandleImpl(ledgerId, index, inputStream, executor);
}