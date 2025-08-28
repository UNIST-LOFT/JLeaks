  public BackupResponse backup(BackupOptions options) throws IOException {
    String dir = options.getTargetDirectory();
    if (dir == null) {
      dir = Configuration.get(PropertyKey.MASTER_BACKUP_DIRECTORY);
    }
    UnderFileSystem ufs;
    if (options.isLocalFileSystem()) {
      ufs = UnderFileSystem.Factory.create("/", UnderFileSystemConfiguration.defaults());
      LOG.info("Backing up to local filesystem in directory {}", dir);
    } else {
      ufs = UnderFileSystem.Factory.createForRoot();
      LOG.info("Backing up to root UFS in directory {}", dir);
    }
    if (!ufs.isDirectory(dir)) {
      if (!ufs.mkdirs(dir, MkdirsOptions.defaults().setCreateParent(true))) {
        throw new IOException(String.format("Failed to create directory %s", dir));
      }
    }
    String backupFilePath;
    try (LockResource lr = new LockResource(mMasterContext.pauseStateLock())) {
      Instant now = Instant.now();
      String backupFileName = String.format("alluxio-backup-%s-%s.gz",
          DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneId.of("UTC")).format(now),
          now.toEpochMilli());
      backupFilePath = PathUtils.concatPath(dir, backupFileName);
      OutputStream ufsStream = ufs.create(backupFilePath);
      try {
        mMasterContext.getBackupManager().backup(ufsStream);
      } catch (Throwable t) {
        try {
          ufsStream.close();
        } catch (Throwable t2) {
          LOG.error("Failed to close backup stream to {}", backupFilePath, t2);
          t.addSuppressed(t2);
        }
        try {
          ufs.deleteFile(backupFilePath);
        } catch (Throwable t2) {
          LOG.error("Failed to clean up partially-written backup at {}", backupFilePath, t2);
          t.addSuppressed(t2);
        }
        throw t;
      }
    }
    String rootUfs = Configuration.get(PropertyKey.MASTER_MOUNT_TABLE_ROOT_UFS);
    if (options.isLocalFileSystem()) {
      rootUfs = "file:///";
    }
    AlluxioURI backupUri = new AlluxioURI(new AlluxioURI(rootUfs), new AlluxioURI(backupFilePath));
    return new BackupResponse(backupUri,
        NetworkAddressUtils.getConnectHost(ServiceType.MASTER_RPC));
  }
