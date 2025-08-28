  protected byte[] fromGzipFile(Path inputFile) {
    try {
      FileInputStream fis = new FileInputStream(inputFile.toFile());
      GZIPInputStream gis = new GZIPInputStream(fis);
      byte[] data = IOUtils.toByteArray(gis);
      return data;
    } catch (IOException e) {
      throw new BatfishException("Failed to gunzip file: " + inputFile, e);
    }
  }
