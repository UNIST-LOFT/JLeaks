public void close() throws IOException {
    if (out != null) {
      OutputStream outShadow = this.out;
      try {
        finish();
        outShadow.close();
        outShadow = null;
      } finally {
        IOUtils.closeStream(outShadow);
      }
    }
  }