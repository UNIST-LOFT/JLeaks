public OutputStream create(String path) throws IOException {
    FileOutputStream stream = new FileOutputStream(path);
    try {
      setPermission(path, "777");
    } catch (IOException e) {
      stream.close();
      throw e;
    }
    CommonUtils.setLocalFileStickyBit(path);
    return stream;
  }