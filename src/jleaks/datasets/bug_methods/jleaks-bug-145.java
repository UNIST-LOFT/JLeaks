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
    Config runtime = Config.newBuilder()
        .put(Keys.topologyId(), topology.getId())
        .put(Keys.topologyName(), topology.getName())
        .put(Keys.topologyDefinition(), topology)
        .put(Keys.schedulerStateManagerAdaptor(), new SchedulerStateManagerAdaptor(statemgr))
        .put(Keys.numContainers(), 1 + TopologyUtils.getNumContainers(topology))
        .build();

    // get a packed plan and schedule it
    packing.initialize(config, runtime);
    PackingPlan packedPlan = packing.pack();

    // TO DO - investigate whether the heron executors can be started
    // in scheduler.schedule method - rather than in scheduler.initialize method
    Config ytruntime = Config.newBuilder()
        .putAll(runtime)
        .put(Keys.instanceDistribution(), TopologyUtils.packingToString(packedPlan))
        .put(Keys.schedulerShutdown(), new Shutdown())
        .build();

    // create an instance of scheduler 
    String schedulerClass = Context.schedulerClass(config);
    IScheduler scheduler = (IScheduler) Class.forName(schedulerClass).newInstance();

    // initialize the scheduler
    scheduler.initialize(config, ytruntime);

    // start the scheduler REST endpoint for receiving requests
    SchedulerServer server = runServer(ytruntime, scheduler, schedulerServerPort);

    // write the scheduler location to state manager.
    setSchedulerLocation(runtime, server);

    // schedule the packed plan
    scheduler.schedule(packedPlan);

    // wait until kill request or some interrupt occurs
    LOG.info("Waiting for termination... ");
    Runtime.schedulerShutdown(ytruntime).await();

    // stop the server and close the state manager
    LOG.info("Shutting down topology: " + topology.getName());
    server.stop();
    statemgr.close();
    System.exit(0);
  }
