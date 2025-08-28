public TsFileSequenceReader(String file, boolean loadMetadataSize) throws IOException {
    this.file = file;
    final Path path = Paths.get(file);
    tsFileInput = new DefaultTsFileInput(path);
    try {
      if (loadMetadataSize) {
       loadMetadataSize();
      }
    } catch (Throwable e) {
      tsFileInput.close();
      throw e;
    }
  }