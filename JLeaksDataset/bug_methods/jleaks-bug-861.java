    public FileInfo stat(String bucket, String fileKey) throws QiniuException {
        Response r = rsGet(bucket, String.format("/stat/%s", encodedEntry(bucket, fileKey)));
        return r.jsonToObject(FileInfo.class);
    }
