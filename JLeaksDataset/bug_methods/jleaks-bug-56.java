        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          String name = relativePath + file.getFileName();
          if (!JarFile.MANIFEST_NAME.equals(name)) {
            final OutputStream out = Files.newOutputStream(zipfs.getPath(name));
            FileUtils.copyFile(file.toFile(), out);
            out.close();
          }
          return super.visitFile(file, attrs);
        }
