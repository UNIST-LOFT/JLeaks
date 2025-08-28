  protected static List<String> getDatabasesHelper(final IMetaStoreClient mClient) throws TException {
    try {
      return mClient.getAllDatabases();
    } catch (TException e) {
      logger.warn("Failure while attempting to get hive databases", e);
      mClient.reconnect();
      return mClient.getAllDatabases();
    }
  }
