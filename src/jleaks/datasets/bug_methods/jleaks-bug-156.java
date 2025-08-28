  public void extract() throws IOException {
    long count = 0;
    Files.createDirectories(outputDir);

    if (Files.list(outputDir).count() > 0) {
      throw new IOException("The output directory must be empty: " + outputDir);
    }

    try (DirectoryStream<Path> stream = Files.newDirectoryStream(reutersDir, "*.sgm")) {
      for (Path sgmFile : stream) {
        extractFile(sgmFile);
        count++;
      }
    }
    if (count == 0) {
      throw new IOException("No .sgm files in " + reutersDir);
    }
  }
