  static List<SnapshotRegionManifest> loadRegionManifests(final Configuration conf,
      final Executor executor, final FileSystem fs, final Path snapshotDir,
      final SnapshotDescription desc, final int manifestSizeLimit) throws IOException {
    FileStatus[] manifestFiles = CommonFSUtils.listStatus(fs, snapshotDir, new PathFilter() {
      @Override
      public boolean accept(Path path) {
        return path.getName().startsWith(SNAPSHOT_MANIFEST_PREFIX);
      }
    });

    if (manifestFiles == null || manifestFiles.length == 0) return null;

    final ExecutorCompletionService<SnapshotRegionManifest> completionService =
      new ExecutorCompletionService<>(executor);
    for (final FileStatus st: manifestFiles) {
      completionService.submit(new Callable<SnapshotRegionManifest>() {
        @Override
        public SnapshotRegionManifest call() throws IOException {
          FSDataInputStream stream = fs.open(st.getPath());
          CodedInputStream cin = CodedInputStream.newInstance(stream);
          cin.setSizeLimit(manifestSizeLimit);

          try {
            return SnapshotRegionManifest.parseFrom(cin);
          } finally {
            stream.close();
          }
        }
      });
    }

    ArrayList<SnapshotRegionManifest> regionsManifest = new ArrayList<>(manifestFiles.length);
    try {
      for (int i = 0; i < manifestFiles.length; ++i) {
        regionsManifest.add(completionService.take().get());
      }
    } catch (InterruptedException e) {
      throw new InterruptedIOException(e.getMessage());
    } catch (ExecutionException e) {
      Throwable t = e.getCause();

      if(t instanceof InvalidProtocolBufferException) {
        throw (InvalidProtocolBufferException)t;
      } else {
        throw new IOException("ExecutionException", e.getCause());
      }
    }
    return regionsManifest;
  }
