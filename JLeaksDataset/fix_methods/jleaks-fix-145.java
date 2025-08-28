public static void runScheduler(
    Config config, int schedulerServerPort, TopologyAPI.Topology topology) throws
    ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
    // create an instance of state manager
    String statemgrClass = Context.stateManagerClass(config);
    IStateManager statemgr = (IStateManager) Class.forName(statemgrClass).newInstance();
    // initialize the state manager
    statemgr.initialize(config);
    // create an instance of the packing class
    String packingClass = Context.packingClass(config);
    IPacking packing = (IPacking) Class.forName(packingClass).newInstance();
    // build the runtime config
    Config runtime = Config.newBuilder().put(Keys.topologyId(), topology.getId()).put(Keys.topologyName(), topology.getName()).put(Keys.topologyDefinition(), topology).put(Keys.schedulerStateManagerAdaptor(), new SchedulerStateManagerAdaptor(statemgr)).put(Keys.numContainers(), 1 + TopologyUtils.getNumContainers(topology)).build();
    SchedulerServer server = null;
    // Put it in a try block so that we can always clean resources
    try {
        // get a packed plan and schedule it
        packing.initialize(config, runtime);
        PackingPlan packedPlan = packing.pack();
        // TODO - investigate whether the heron executors can be started
        // in scheduler.schedule method - rather than in scheduler.initialize method
        Config ytruntime = Config.newBuilder().putAll(runtime).put(Keys.instanceDistribution(), TopologyUtils.packingToString(packedPlan)).put(Keys.schedulerShutdown(), new Shutdown()).build();
        // create an instance of scheduler
        String schedulerClass = Context.schedulerClass(config);
        IScheduler scheduler = (IScheduler) Class.forName(schedulerClass).newInstance();
        // initialize the scheduler
        scheduler.initialize(config, ytruntime);
        // start the scheduler REST endpoint for receiving requests
        server = runServer(ytruntime, scheduler, schedulerServerPort);
        // write the scheduler location to state manager.
        setSchedulerLocation(runtime, server);
        // schedule the packed plan
        scheduler.schedule(packedPlan);
        // wait until kill request or some interrupt occurs
        LOG.info("Waiting for termination... ");
        Runtime.schedulerShutdown(ytruntime).await();
    } catch (Exception e) {
        // Log and exit the process
        LOG.log(Level.SEVERE, "Failed to run scheduler for topology: {0}. Existing", topology.getName());
        System.exit(1);
    } finally {
        // Clean the resources
        if (server != null) {
            server.stop();
        }
        statemgr.close();
    }
    // stop the server and close the state manager
    LOG.log(Level.INFO, "Shutting down topology: {0}", topology.getName());
    System.exit(0);
}