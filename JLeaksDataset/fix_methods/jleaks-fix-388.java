public void start(final ServiceProvider<Service> serviceProvider) 
{
    try {
        Properties properties = new Properties();
        properties.put(ConnectionPropertyNames.CONNECTION_NAME, CONNECTION_PREFIX + this.entityIdentifier);
        clusterConnection = ConnectionFactory.connect(clusterUri, properties);
    } catch (ConnectionException ex) {
        throw new RuntimeException(ex);
    }
    entityFactory = new EhcacheClientEntityFactory(clusterConnection);
    try {
        EhcacheEntityCreationException failure = null;
        if (autoCreate) {
            try {
                entityFactory.create(entityIdentifier, serverConfiguration);
            } catch (EhcacheEntityCreationException e) {
                failure = e;
            } catch (EntityAlreadyExistsException e) {
                // ignore - entity already exists
            }
        }
        try {
            entity = entityFactory.retrieve(entityIdentifier, serverConfiguration);
        } catch (EntityNotFoundException e) {
            /*
         * If the connection failed because of a creation failure, re-throw the creation failure.
         */
            throw new IllegalStateException(failure == null ? e : failure);
        }
    } catch (RuntimeException e) {
        if (entityFactory != null) {
            entityFactory.abandonLeadership(entityIdentifier);
            entityFactory.close();
            entityFactory = null;
        }
        try {
            clusterConnection.close();
            clusterConnection = null;
        } catch (IOException ex) {
            LOGGER.info("Error closing cluster connection: " + ex);
        }
        throw e;
    }
}