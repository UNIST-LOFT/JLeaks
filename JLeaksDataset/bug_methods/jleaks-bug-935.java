		public Mono<Void> transferTo(File dest) {
			if (this.storage == null || !getFilename().isPresent()) {
				return Mono.error(new IllegalStateException("The part does not represent a file."));
			}
			try {
				ReadableByteChannel ch = Channels.newChannel(this.storage.getInputStream());
				long expected = (ch instanceof FileChannel ? ((FileChannel) ch).size() : Long.MAX_VALUE);
				long actual = new FileOutputStream(dest).getChannel().transferFrom(ch, 0, expected);
				if (actual < expected) {
					return Mono.error(new IOException(
							"Could only write " + actual + " out of " + expected + " bytes"));
				}
			}
			catch (IOException ex) {
				return Mono.error(ex);
			}
			return Mono.empty();
		}
