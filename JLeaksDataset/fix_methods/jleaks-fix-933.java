protected Set<Resource> doFindPathMatchingFileResources(Resource rootDirResource, String subPattern){
    URI rootDirUri;
    String rootDir;
    try {
        rootDirUri = rootDirResource.getURI();
        rootDir = rootDirUri.getPath();
        // If the URI is for a "resource" in the GraalVM native image file system, we have to
        // ensure that the root directory does not end in a slash while simultaneously ensuring
        // that the root directory is not an empty string (since fileSystem.getPath("").resolve(str)
        // throws an ArrayIndexOutOfBoundsException in a native image).
        if ("resource".equals(rootDirUri.getScheme()) && (rootDir.length() > 1) && rootDir.endsWith("/")) {
            rootDir = rootDir.substring(0, rootDir.length() - 1);
        }
    } catch (Exception ex) {
        if (logger.isInfoEnabled()) {
            logger.info("Failed to resolve %s in the file system: %s".formatted(rootDirResource, ex));
        }
        return Collections.emptySet();
    }
    FileSystem fileSystem = getFileSystem(rootDirUri);
    if (fileSystem == null) {
        return Collections.emptySet();
    }
    try {
        Path rootPath = fileSystem.getPath(rootDir);
        String resourcePattern = rootPath.resolve(subPattern).toString();
        Predicate<Path> resourcePatternMatches = path -> getPathMatcher().match(resourcePattern, path.toString());
        if (logger.isTraceEnabled()) {
            logger.trace("Searching directory [%s] for files matching pattern [%s]".formatted(rootPath.toAbsolutePath(), subPattern));
        }
        Set<Resource> result = new LinkedHashSet<>();
        try (Stream<Path> files = Files.walk(rootPath)) {
            files.filter(resourcePatternMatches).sorted().forEach(file -> {
                try {
                    result.add(convertToResource(file.toUri()));
                } catch (Exception ex) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Failed to convert file %s to an org.springframework.core.io.Resource: %s".formatted(file, ex));
                    }
                }
            });
        } catch (Exception ex) {
            if (logger.isDebugEnabled()) {
                logger.debug("Faild to complete search in directory [%s] for files matching pattern [%s]: %s".formatted(rootPath.toAbsolutePath(), subPattern, ex));
            }
        }
        return result;
    } finally {
        try {
            fileSystem.close();
        } catch (UnsupportedOperationException ex) {
            // ignore
        }
    }
}