  public void close() throws IOException {
    out.close();
    if (out != err) {
      err.close();
    }
  }
