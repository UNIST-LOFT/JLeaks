  public void close() throws IOException {
    fileOutErr.close();
    if (actionFileSystem instanceof Closeable) {
      ((Closeable) actionFileSystem).close();
    }
  }
