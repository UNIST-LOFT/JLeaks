  public void putFile(String path, byte[] data) throws IOException {
    InputStream is = new ByteArrayInputStream(data);
    putFile(path, is);
    IOUtils.closeQuietly(is);
  }
