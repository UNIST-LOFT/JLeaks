public void executeUpgradeInfos(
		String bundleSymbolicName, List<UpgradeInfo> upgradeInfos) {
    OutputStreamContainerFactory outputStreamContainerFactory = _outputStreamContainerFactoryTracker.getOutputStreamContainerFactory();
    OutputStreamContainer outputStreamContainer = outputStreamContainerFactory.create("upgrade-" + bundleSymbolicName);
    Release release = _releaseLocalService.fetchRelease(bundleSymbolicName);
    if (release != null) {
        _releasePublisher.publishInProgress(release);
    }
    try (OutputStream outputStream = outputStreamContainer.getOutputStream()) {
        _outputStreamContainerFactoryTracker.runWithSwappedLog(new UpgradeInfosRunnable(bundleSymbolicName, upgradeInfos, outputStream), outputStreamContainer.getDescription(), outputStream);
    } catch (IOException ioe) {
        throw new RuntimeException(ioe);
    }
    release = _releaseLocalService.fetchRelease(bundleSymbolicName);
    if (release != null) {
        _releasePublisher.publish(release);
    }
}