protected void serviceStop() throws Exception {
    if (registryClient != null) {
      registryClient.stop();
    }
    fs.getFileSystem().close();
    super.serviceStop();
  }