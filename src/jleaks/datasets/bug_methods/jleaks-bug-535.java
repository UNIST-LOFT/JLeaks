	public List<SpoonFile> getFiles() {
		// Indexing content
		if (files == null) {
			files = new ArrayList<>();
			try (ZipInputStream zipInput = new ZipInputStream(new BufferedInputStream(new FileInputStream(file)))) {
				ZipEntry entry;
				while ((entry = zipInput.getNextEntry()) != null) {
					// deflate in buffer
					final int buffer = 2048;
					ByteArrayOutputStream output = new ByteArrayOutputStream(buffer);
					int count;
					byte[] data = new byte[buffer];
					while ((count = zipInput.read(data, 0, buffer)) != -1) {
						output.write(data, 0, count);
					}
					output.flush();
					output.close();

					files.add(new ZipFile(this, entry.getName(), output
							.toByteArray()));
				}
			} catch (Exception e) {
				Launcher.LOGGER.error(e.getMessage(), e);
			}
		}
		return files;
	}
