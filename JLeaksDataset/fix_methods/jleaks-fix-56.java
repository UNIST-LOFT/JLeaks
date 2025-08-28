
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          String name = relativePath + file.getFileName();
          if (!JarFile.MANIFEST_NAME.equals(name)) {
            try (final OutputStream out = Files.newOutputStream(zipfs.getPath(name))) {
              FileUtils.copyFile(file.toFile(), out);
            }
          }
          return super.visitFile(file, attrs);
        }