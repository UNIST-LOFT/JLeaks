      public void close() throws IOException {
        if (!closed) {
          closed = true;
          onClose(path, this);
        }
        super.close();
      }
