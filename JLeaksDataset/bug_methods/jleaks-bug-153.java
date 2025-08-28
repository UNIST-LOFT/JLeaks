  public TsFileSequenceReader(TsFileInput input, boolean loadMetadataSize)
      throws IOException {
    this.tsFileInput = input;
    if (loadMetadataSize) { // NOTE no autoRepair here
      loadMetadataSize();
    }
  }
