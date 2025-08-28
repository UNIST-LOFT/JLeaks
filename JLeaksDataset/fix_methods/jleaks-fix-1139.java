public List<Long> splitOffsets() {
    Preconditions.checkState(isClosed, "File is not yet closed");
    try (Reader reader = ORC.newFileReader(file.toInputFile(), conf)) {
      List<StripeInformation> stripes = reader.getStripes();
      return Collections.unmodifiableList(Lists.transform(stripes, StripeInformation::getOffset));
    } catch (IOException e) {
      throw new RuntimeIOException(e, "Can't close ORC reader %s", file.location());
    }
  }