private synchronized String ufsFingerprint(long fileId) throws IOException 
{
    FileInfo fileInfo = mBlockWorker.getFileInfo(fileId);
    String dstPath = fileInfo.getUfsPath();
    try (CloseableResource<UnderFileSystem> ufsResource = mUfsManager.get(fileInfo.getMountId()).acquireUfsResource()) {
        UnderFileSystem ufs = ufsResource.get();
        return ufs.isFile(dstPath) ? ufs.getFingerprint(dstPath) : null;
    }
}