public FileInfo stat(String bucket, String fileKey) throws QiniuException 
{
    Response res = rsGet(bucket, String.format("/stat/%s", encodedEntry(bucket, fileKey)));
    if (!res.isOK()) {
        throw new QiniuException(res);
    }
    FileInfo fileInfo = res.jsonToObject(FileInfo.class);
    res.close();
    return fileInfo;
}