	public boolean get(final String remotePath, final InputStreamCallback callback) {
		Assert.notNull(remotePath, "'remotePath' cannot be null");
		return this.execute(session -> {
			InputStream inputStream = session.readRaw(remotePath);
			callback.doWithInputStream(inputStream);
			inputStream.close();
			return session.finalizeRaw();
		});
	}
