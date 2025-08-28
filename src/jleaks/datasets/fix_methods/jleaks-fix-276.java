public void execute() throws TException, AppFabricServiceException 
{
    Preconditions.checkNotNull(command, "App client is not configured to run");
    Preconditions.checkNotNull(configuration, "App client configuration is not set");
    String address = "localhost";
    int port = configuration.getInt(Constants.CFG_APP_FABRIC_SERVER_PORT, Constants.DEFAULT_APP_FABRIC_SERVER_PORT);
    TTransport transport = null;
    TProtocol protocol = null;
    try {
        transport = new TFramedTransport(new TSocket(address, port));
        protocol = new TBinaryProtocol(transport);
        AppFabricService.Client client = new AppFabricService.Client(protocol);
        if ("help".equals(command)) {
            return;
        }
        if ("deploy".equals(command)) {
            AuthToken dummyAuthToken = new AuthToken("AppFabricClient");
            ResourceIdentifier resourceIdentifier = new ResourceIdentifier("Account", "Application", this.resource, 0);
            client.deploy(dummyAuthToken, resourceIdentifier);
            LOG.info("Deployed: " + resource);
            return;
        }
        if ("start".equals(command)) {
            AuthToken dummyAuthToken = new AuthToken("AppFabricClient");
            FlowIdentifier identifier = new FlowIdentifier("Account", application, processor, 0);
            RunIdentifier runIdentifier = client.start(dummyAuthToken, new FlowDescriptor(identifier, new ArrayList<String>()));
            Preconditions.checkNotNull(runIdentifier, "Problem starting the application");
            LOG.info("Started application with id: " + runIdentifier.getId());
            return;
        }
        if ("stop".equals(command)) {
            AuthToken dummyAuthToken = new AuthToken("AppFabricClient");
            FlowIdentifier identifier = new FlowIdentifier("Account", application, processor, 0);
            RunIdentifier runIdentifier = client.stop(dummyAuthToken, identifier);
            Preconditions.checkNotNull(runIdentifier, "Problem stopping the application");
            LOG.info("Stopped application running with id: " + runIdentifier.getId());
        }
        if ("status".equals(command)) {
            AuthToken dummyAuthToken = new AuthToken("AppFabricClient");
            FlowIdentifier identifier = new FlowIdentifier("Account", application, processor, 0);
            FlowStatus flowStatus = client.status(dummyAuthToken, identifier);
            Preconditions.checkNotNull(flowStatus, "Problem getting the status the application");
            LOG.info(flowStatus.toString());
        }
        if ("verify".equals(command)) {
            Location location = new LocalLocationFactory().create(this.resource);
            final Injector injector = Guice.createInjector(new BigMamaModule(configuration), new DataFabricModules().getInMemoryModules());
            ManagerFactory factory = injector.getInstance(ManagerFactory.class);
            Manager<Location, ApplicationWithPrograms> manager = (Manager<Location, ApplicationWithPrograms>) factory.create();
            manager.deploy(new Id.Account("Account"), location);
        }
    } catch (Exception e) {
        LOG.info("Caught Exception while verifying application");
        throw Throwables.propagate(e);
    } finally {
        transport.close();
    }
    LOG.info("Verification succeeded");
    if ("deploy".equals(command)) {
        // TODO: Deploy
    }
}