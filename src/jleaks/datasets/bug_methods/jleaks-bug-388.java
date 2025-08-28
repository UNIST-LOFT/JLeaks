  public void start(final ServiceProvider<Service> serviceProvider) {
    try {
      Properties properties = new Properties();
      properties.put(ConnectionPropertyNames.CONNECTION_NAME, CONNECTION_PREFIX + this.entityIdentifier);
      clusterConnection = ConnectionFactory.connect(clusterUri, properties);
    } catch (ConnectionException ex) {
      throw new RuntimeException(ex);
    }
    entityFactory = new EhcacheClientEntityFactory(clusterConnection);
    if (autoCreate) {
      try {
        create();
      } catch (IllegalStateException ex) {
        //ignore - entity already exists
      }
    }
    connect();
  }
