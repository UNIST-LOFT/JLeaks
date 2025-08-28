  protected byte[] fromGzipFile(Path inputFile) {
    try {
      // Awkward nested try blocks required because we refuse to throw IOExceptions.
      try (Closer closer = Closer.create()) {
        FileInputStream fis = closer.register(new FileInputStream(inputFile.toFile()));
        GZIPInputStream gis = closer.register(new GZIPInputStream(fis));
        return IOUtils.toByteArray(gis);
      }
    } catch (IOException e) {
      throw new BatfishException("Failed to gunzip file: " + inputFile, e);
    }
  }
