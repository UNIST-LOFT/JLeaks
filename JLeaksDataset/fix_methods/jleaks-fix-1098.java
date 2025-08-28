public synchronized void close() throws IOException {
    if (closed) {
      return;
    }
    try {
      try {
        flush();
      } finally {
        if (closeOutputStream) {
          super.close();
          codec.close();
        }
        freeBuffers();
      }
    } finally {
      closed = true;
    }
  }