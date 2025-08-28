  public void close() throws IOException {
    if (this.connection != null) {
      this.connection.close();
    }
  }
