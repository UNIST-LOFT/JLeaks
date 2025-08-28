  private synchronized String ufsFingerprint(long fileId) throws IOException {
    FileInfo fileInfo = mBlockWorker.getFileInfo(fileId);
    String dstPath = fileInfo.getUfsPath();
    UnderFileSystem ufs = mUfsManager.get(fileInfo.getMountId()).getUfs();
    return ufs.isFile(dstPath) ? ufs.getFingerprint(dstPath) : null;
  }
