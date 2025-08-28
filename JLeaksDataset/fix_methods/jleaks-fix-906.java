
	public void checkForUpdates() {
		updatesAvailable = false;
		boolean oss = BuildConfig.FLAVOR.equalsIgnoreCase("oss");

		if (oss && !fetchReleaseNotes) {
			// OSS version update checker is the responsibility of FDROID
			Log.d(TAG, "OSS version - will not be checking for updates.");
		} else {
			try (WebStream webStream = new WebStream(BuildConfig.SKYTUBE_UPDATES_URL)) {
				String updatesJSONStr = webStream.downloadRemoteTextFile();

				JSONObject json = new JSONObject(updatesJSONStr);
				latestApkVersion = getLatestVersionNumber(json);
				releaseNotes = getReleaseNotes(json);

				Log.d(TAG, "CURRENT_VER: " + currentVersionNumber);
				Log.d(TAG, "REMOTE_VER: " + latestApkVersion);

				if (!oss) {
					if (!Objects.equals(currentVersionNumber, latestApkVersion)) {
						this.latestApkUrl = getLatestApkUrl(json);
						updatesAvailable = latestApkUrl != null;
						Log.d(TAG, "Update available.  APK_URL: " + latestApkUrl);
					} else {
						Log.d(TAG, "Not updating.");
					}
				}
			} catch (Throwable e) {
				Log.e(TAG, "An error has occurred while checking for updates", e);
			}
		}
	}
