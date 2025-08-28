	private void closeRepository(String repositoryName) {
		Repository repository = getRepository(repositoryName);
		// assume 2 uses in case reflection fails
		int uses = 2;
		try {
			// The FileResolver caches repositories which is very useful
			// for performance until you want to delete a repository.
			// I have to use reflection to call close() the correct
			// number of times to ensure that the object and ref databases
			// are properly closed before I can delete the repository from
			// the filesystem.
			Field useCnt = Repository.class.getDeclaredField("useCnt");
			useCnt.setAccessible(true);
			uses = ((AtomicInteger) useCnt.get(repository)).get();
		} catch (Exception e) {
			logger.warn(MessageFormat
					.format("Failed to reflectively determine use count for repository {0}",
							repositoryName), e);
		}
		if (uses > 0) {
			logger.info(MessageFormat
					.format("{0}.useCnt={1}, calling close() {2} time(s) to close object and ref databases",
							repositoryName, uses, uses));
			for (int i = 0; i < uses; i++) {
				repository.close();
			}
		}
	}
