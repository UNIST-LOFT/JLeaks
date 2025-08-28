	private String compileToExplodedBundle(PrintWriter writer) throws IOException, OSGiException {
		compileAttempted();

		Files.createDirectories(binaryDir);

		Summary summary = new Summary();

		List<String> options = new ArrayList<>();
		options.add("-g");
		options.add("-d");
		options.add(binaryDir.toString());
		options.add("-sourcepath");
		options.add(getSourceDirectory().toString());
		options.add("-classpath");
		options.add(
			System.getProperty("java.class.path") + File.pathSeparator + binaryDir.toString());
		options.add("-proc:none");

		BundleJavaManager bundleJavaManager = createBundleJavaManager(writer, summary, options);

		final List<ResourceFileJavaFileObject> sourceFiles = newSources.stream()
				.map(sf -> new ResourceFileJavaFileObject(sf.getParentFile(), sf, Kind.SOURCE))
				.collect(Collectors.toList());

		Path binaryManifest = getBinaryManifestPath();
		if (Files.exists(binaryManifest)) {
			Files.delete(binaryManifest);
		}

		// try to compile, if we fail, avoid offenders and try again
		while (!sourceFiles.isEmpty()) {
			if (tryBuild(writer, bundleJavaManager, sourceFiles, options)) {
				break;
			}
		}

		// mark the successful compilations
		for (ResourceFileJavaFileObject sourceFile : sourceFiles) {
			buildSuccess(sourceFile.getFile());
		}
		// buildErrors is now up to date, set status
		if (getBuildErrorCount() > 0) {
			int count = getBuildErrorCount();
			summary.printf("%d source file%s with errors", count, count > 1 ? "s" : "");
		}

		ResourceFile sourceManifest = getSourceManifestFile();
		if (sourceManifest.exists()) {
			Files.createDirectories(binaryManifest.getParent());
			try (InputStream inStream = sourceManifest.getInputStream()) {
				Files.copy(inStream, binaryManifest, StandardCopyOption.REPLACE_EXISTING);
			}
			return summary.getValue();
		}

		return generateManifest(writer, summary, binaryManifest);
	}
