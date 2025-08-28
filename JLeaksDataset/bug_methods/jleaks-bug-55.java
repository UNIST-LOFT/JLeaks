  public static void createJar(File dir, File jarFile) throws IOException {

    final Map<String, ?> env = Collections.singletonMap("create", "true");
    if (jarFile.exists() && !jarFile.delete()) {
      throw new RuntimeException("Failed to remove " + jarFile);
    }
    URI uri = URI.create("jar:" + jarFile.toURI());
    try (final FileSystem zipfs = FileSystems.newFileSystem(uri, env);) {

      File manifestFile = new File(dir, JarFile.MANIFEST_NAME);
      Files.createDirectory(zipfs.getPath("META-INF"));
      final OutputStream out = Files.newOutputStream(zipfs.getPath(JarFile.MANIFEST_NAME));
      if (!manifestFile.exists()) {
        new Manifest().write(out);
      } else {
        FileUtils.copyFile(manifestFile, out);
      }
      out.close();

      final java.nio.file.Path root = dir.toPath();
      Files.walkFileTree(root, new java.nio.file.SimpleFileVisitor<Path>() {
        String relativePath;

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
            throws IOException {
          relativePath = root.relativize(dir).toString();
          if (!relativePath.isEmpty()) {
            if (!relativePath.endsWith("/")) {
              relativePath += "/";
            }
            if (!relativePath.equals("META-INF/")) {
              final Path dstDir = zipfs.getPath(relativePath);
              Files.createDirectory(dstDir);
            }
          }
          return super.preVisitDirectory(dir, attrs);
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          String name = relativePath + file.getFileName();
          if (!JarFile.MANIFEST_NAME.equals(name)) {
            final OutputStream out = Files.newOutputStream(zipfs.getPath(name));
            FileUtils.copyFile(file.toFile(), out);
            out.close();
          }
          return super.visitFile(file, attrs);
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
          relativePath = root.relativize(dir.getParent()).toString();
          if (!relativePath.isEmpty() && !relativePath.endsWith("/")) {
            relativePath += "/";
          }
          return super.postVisitDirectory(dir, exc);
        }
      });
    }
  }
