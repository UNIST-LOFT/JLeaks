  @Override public void close() throws IOException {
    writeChunk(new IEND());
    out.close();
  }
