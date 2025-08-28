	protected Set<Resource> doFindPathMatchingFileResources(Resource rootDirResource, String subPattern)
			throws IOException {

		URI rootDirUri = rootDirResource.getURI();
		String rootDir = rootDirUri.getPath();
		// If the URI is for a "resource" in the GraalVM native image file system, we have to
		// ensure that the root directory does not end in a slash while simultaneously ensuring
		// that the root directory is not an empty string (since fileSystem.getPath("").resolve(str)
		// throws an ArrayIndexOutOfBoundsException in a native image).
		if ("resource".equals(rootDirUri.getScheme()) && (rootDir.length() > 1) && rootDir.endsWith("/")) {
			rootDir = rootDir.substring(0, rootDir.length() - 1);
		}

		FileSystem fileSystem;
		try {
			fileSystem = FileSystems.getFileSystem(rootDirUri.resolve("/"));
		}
		catch (Exception ex) {
			fileSystem = FileSystems.newFileSystem(rootDirUri.resolve("/"), Map.of(),
					ClassUtils.getDefaultClassLoader());
		}

		Path rootPath = fileSystem.getPath(rootDir);
		String resourcePattern = rootPath.resolve(subPattern).toString();
		Predicate<Path> resourcePatternMatches = path -> getPathMatcher().match(resourcePattern, path.toString());
		Set<Resource> result = new HashSet<>();
		try (Stream<Path> files = Files.walk(rootPath)) {
			files.filter(resourcePatternMatches).sorted().forEach(file -> {
				try {
					result.add(convertToResource(file.toUri()));
				}
				catch (Exception ex) {
					// TODO Introduce logging
				}
			});
		}
		catch (NoSuchFileException ex) {
			// TODO Introduce logging
		}
		try {
			fileSystem.close();
		}
		catch (UnsupportedOperationException ex) {
			// TODO Introduce logging
		}
		return result;
	}
