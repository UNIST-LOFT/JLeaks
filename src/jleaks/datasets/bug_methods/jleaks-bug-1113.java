  public void close() throws IOException {
    if (out != null) {
      OutputStream outShadow = this.out;
      finish();
      outShadow.close();
    }
  }
