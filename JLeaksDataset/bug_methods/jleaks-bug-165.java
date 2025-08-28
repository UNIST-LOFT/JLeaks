    public void close() throws IOException {
      // only close the file if it has not been closed yet
      if (isOpen) {
        super.close();
        file.close();
        isOpen = false;
      }
    }
