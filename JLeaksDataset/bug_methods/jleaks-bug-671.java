	public void executeUpgradeInfos(
		String bundleSymbolicName, List<UpgradeInfo> upgradeInfos) {

		OutputStreamContainerFactory outputStreamContainerFactory =
			_outputStreamContainerFactoryTracker.
				getOutputStreamContainerFactory();

		OutputStreamContainer outputStreamContainer =
			outputStreamContainerFactory.create(
				"upgrade-" + bundleSymbolicName);

		OutputStream outputStream = outputStreamContainer.getOutputStream();

		Release release = _releaseLocalService.fetchRelease(bundleSymbolicName);

		if (release != null) {
			_releasePublisher.publishInProgress(release);
		}

		_outputStreamContainerFactoryTracker.runWithSwappedLog(
			new UpgradeInfosRunnable(
				bundleSymbolicName, upgradeInfos, outputStream),
			outputStreamContainer.getDescription(), outputStream);

		try {
			outputStream.close();
		}
		catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}

		release = _releaseLocalService.fetchRelease(bundleSymbolicName);

		if (release != null) {
			_releasePublisher.publish(release);
		}
	}
