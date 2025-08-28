  public List<Long> splitOffsets() {
    Preconditions.checkState(isClosed, "File is not yet closed");
    String fileLoc = file.location();
    Reader reader;
    try {
      reader = OrcFile.createReader(new Path(fileLoc), new OrcFile.ReaderOptions(conf));
    } catch (IOException ioe) {
      throw new RuntimeIOException(ioe, "Cannot read file " + fileLoc);
    }

    List<StripeInformation> stripes = reader.getStripes();
    return Collections.unmodifiableList(Lists.transform(stripes, StripeInformation::getOffset));
  }
