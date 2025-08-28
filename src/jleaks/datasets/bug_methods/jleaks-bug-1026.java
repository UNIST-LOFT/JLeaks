  private WikiPageProperties loadAttributes(final FileVersion fileVersion) throws IOException {
    final WikiPageProperties props = new WikiPageProperties();
    props.loadFromXml(fileVersion.getContent());
    props.setLastModificationTime(fileVersion.getLastModificationTime());
    return props;
  }
