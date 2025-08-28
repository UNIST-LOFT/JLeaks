  public void close() throws IOException {
    if (!myClosed) {
      myClosed = true;
      flush();
      myStorage.close();
    }
  }