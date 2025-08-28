  protected void serviceStop() throws Exception {
    if (registryClient != null) {
      registryClient.stop();
    }
    super.serviceStop();
  }
