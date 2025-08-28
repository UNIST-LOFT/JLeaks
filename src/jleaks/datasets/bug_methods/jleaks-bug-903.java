	public String backupDbsToSdCard() throws IOException {
		SubscriptionsDb     subscriptionsDb = SubscriptionsDb.getSubscriptionsDb();
		BookmarksDb         bookmarksDb = BookmarksDb.getBookmarksDb();
		PlaybackStatusDb    playbackDb = PlaybackStatusDb.getPlaybackStatusDb();
		ChannelFilteringDb  channelFilteringDb = ChannelFilteringDb.getChannelFilteringDb();
		SearchHistoryDb     searchHistoryDb = SearchHistoryDb.getSearchHistoryDb();

		final File          backupPath = new File(EXPORT_DIR, generateFileName());

		Gson gson = new Gson();

		// close the databases
		subscriptionsDb.close();
		bookmarksDb.close();
		playbackDb.close();
		channelFilteringDb.close();
		searchHistoryDb.close();

		ZipOutput databasesZip = new ZipOutput(backupPath);

		// backup the databases inside a zip file
		databasesZip.addFile(subscriptionsDb.getDatabasePath());
		databasesZip.addFile(bookmarksDb.getDatabasePath());
		databasesZip.addFile(playbackDb.getDatabasePath());
		databasesZip.addFile(channelFilteringDb.getDatabasePath());
		databasesZip.addFile(searchHistoryDb.getDatabasePath());

		databasesZip.addContent(PREFERENCES_JSON, gson.toJson(getImportantKeys()));

		databasesZip.close();
		return backupPath.getPath();
	}
