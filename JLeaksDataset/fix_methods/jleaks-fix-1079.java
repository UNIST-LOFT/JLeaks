private void copyFromJar(final Path target) throws URISyntaxException, IOException 
{
    String resourcePath = "/" + SWAGGER_UI_FOLDER_NAME;
    URI resource = Objects.requireNonNull(getClass().getResource(resourcePath), "resource").toURI();
    try (FileSystem fileSystem = FileSystems.newFileSystem(resource, Map.of())) {
        final Path jarPath = fileSystem.getPath(resourcePath);
        try (Stream<Path> stream = Files.walk(jarPath)) {
            stream.forEachOrdered(path -> {
                try {
                    if (Files.isDirectory(path)) {
                        final Path currentTarget = target.resolve(jarPath.relativize(path).toString());
                        Files.createDirectories(currentTarget);
                    } else {
                        Files.copy(path, target.resolve(jarPath.relativize(path).toString()), StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException("Failed to copy " + path, e);
                }
            });
        }
    }
}