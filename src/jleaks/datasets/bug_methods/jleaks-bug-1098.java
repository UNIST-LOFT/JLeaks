  public synchronized void close() throws IOException {
    if (closed) {
      return;
    }
    try {
      flush();
      if (closeOutputStream) {
        super.close();
        codec.close();
      }
      freeBuffers();
    } finally {
      closed = true;
    }
  }
