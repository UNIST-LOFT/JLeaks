  public OutputStream create(String path) throws IOException {
    FileOutputStream stream = new FileOutputStream(path);
    setPermission(path, "777");
    CommonUtils.setLocalFileStickyBit(path);
    return stream;
  }
