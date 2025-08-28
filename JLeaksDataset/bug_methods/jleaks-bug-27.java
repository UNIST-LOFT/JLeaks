  public FileOutStream(AlluxioURI path, OutStreamOptions options, FileSystemContext context,
      UnderFileSystemFileOutStream.Factory underOutStreamFactory) throws IOException {
    mUri = Preconditions.checkNotNull(path);
    mNonce = IdUtils.getRandomNonNegativeLong();
    mBlockSize = options.getBlockSizeBytes();
    mAlluxioStorageType = options.getAlluxioStorageType();
    mUnderStorageType = options.getUnderStorageType();
    mContext = context;
    mUnderOutStreamFactory = underOutStreamFactory;
    mPreviousBlockOutStreams = new LinkedList<>();
    mUfsDelegation = Configuration.getBoolean(PropertyKey.USER_UFS_DELEGATION_ENABLED);
    if (mUnderStorageType.isSyncPersist()) {
      // Get the ufs path from the master.
      FileSystemMasterClient client = mContext.acquireMasterClient();
      try {
        mUfsPath = client.getStatus(mUri).getUfsPath();
      } catch (AlluxioException e) {
        throw new IOException(e);
      } finally {
        mContext.releaseMasterClient(client);
      }
      if (mUfsDelegation) {
        mFileSystemWorkerClient = mContext.createWorkerClient();
        try {
          Permission perm = options.getPermission();
          mUfsFileId =
              mFileSystemWorkerClient.createUfsFile(new AlluxioURI(mUfsPath),
                  CreateUfsFileOptions.defaults().setPermission(perm));
        } catch (AlluxioException e) {
          mFileSystemWorkerClient.close();
          throw new IOException(e);
        }
        mUnderStorageOutputStream = mUnderOutStreamFactory
            .create(mFileSystemWorkerClient.getWorkerDataServerAddress(), mUfsFileId);
      } else {
        String tmpPath = PathUtils.temporaryFileName(mNonce, mUfsPath);
        UnderFileSystem ufs = UnderFileSystem.get(tmpPath);
        // TODO(jiri): Implement collection of temporary files left behind by dead clients.
        CreateOptions createOptions = new CreateOptions().setPermission(options.getPermission());
        mUnderStorageOutputStream = ufs.create(tmpPath, createOptions);

        // Set delegation related vars to null as we are not using worker delegation for ufs ops
        mFileSystemWorkerClient = null;
        mUfsFileId = null;
      }
    } else {
      mUfsPath = null;
      mUnderStorageOutputStream = null;
      mFileSystemWorkerClient = null;
      mUfsFileId = null;
    }
    mClosed = false;
    mCanceled = false;
    mShouldCacheCurrentBlock = mAlluxioStorageType.isStore();
    mBytesWritten = 0;
    mLocationPolicy = Preconditions.checkNotNull(options.getLocationPolicy(),
        PreconditionMessage.FILE_WRITE_LOCATION_POLICY_UNSPECIFIED);
  }
