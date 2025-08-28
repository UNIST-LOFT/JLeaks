private void loadDownloads() 
{
    try (DownloadStateCursor loadedDownloadStates = downloadIndex.getDownloadStates()) {
        while (loadedDownloadStates.moveToNext()) {
            DownloadState downloadState = loadedDownloadStates.getDownloadState();
            downloadStates.put(downloadState.uri, downloadState);
        }
    } catch (IOException e) {
        Log.w(TAG, "Failed to query download states", e);
    }
}