  private void copyFromJar(final Path target) throws URISyntaxException, IOException {
    String resourcePath = "/" + SWAGGER_UI_FOLDER_NAME;
    URI resource = Objects.requireNonNull(getClass().getResource(resourcePath), "resource").toURI();
    FileSystem fileSystem =
        FileSystems.newFileSystem(resource, Collections.<String, String>emptyMap());

    final Path jarPath = fileSystem.getPath(resourcePath);

    Files.walkFileTree(
        jarPath,
        new SimpleFileVisitor<>() {

          @Override
          public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
              throws IOException {
            final Path currentTarget = target.resolve(jarPath.relativize(dir).toString());
            Files.createDirectories(currentTarget);
            return FileVisitResult.CONTINUE;
          }

          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
              throws IOException {
            Files.copy(
                file,
                target.resolve(jarPath.relativize(file).toString()),
                StandardCopyOption.REPLACE_EXISTING);
            return FileVisitResult.CONTINUE;
          }
        });

    fileSystem.close();
  }
