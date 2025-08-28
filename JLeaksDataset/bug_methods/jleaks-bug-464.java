  private void loadDownloads() {
    try {
      DownloadStateCursor loadedDownloadStates = downloadIndex.getDownloadStates();
      while (loadedDownloadStates.moveToNext()) {
        DownloadState downloadState = loadedDownloadStates.getDownloadState();
        downloadStates.put(downloadState.uri, downloadState);
      }
      loadedDownloadStates.close();
    } catch (IOException e) {
      Log.w(TAG, "Failed to query download states", e);
    }
  }

  private void startServiceWithAction(DownloadAction action) {
    DownloadService.startWithAction(
        context, DemoDownloadService.class, action, /* foreground= */ false);
  }

  private DownloadHelper getDownloadHelper(
      Uri uri, String extension, RenderersFactory renderersFactory) {
    int type = Util.inferContentType(uri, extension);
    switch (type) {
      case C.TYPE_DASH:
        return DownloadHelper.forDash(uri, dataSourceFactory, renderersFactory);
      case C.TYPE_SS:
        return DownloadHelper.forSmoothStreaming(uri, dataSourceFactory, renderersFactory);
      case C.TYPE_HLS:
        return DownloadHelper.forHls(uri, dataSourceFactory, renderersFactory);
      case C.TYPE_OTHER:
        return DownloadHelper.forProgressive(uri);
      default:
        throw new IllegalStateException("Unsupported type: " + type);
    }
  }
