public void close(String cause) 
{
    // set stop signal is true
    // execute only once
    if (!Stopper.stop()) {
        logger.warn("MasterServer is already stopped, current cause: {}", cause);
        return;
    }
    // thread sleep 3 seconds for thread quietly stop
    ThreadUtils.sleep(Constants.SERVER_CLOSE_WAIT_TIME.toMillis());
    try (SchedulerApi closedSchedulerApi = schedulerApi;
        MasterSchedulerBootstrap closedSchedulerBootstrap = masterSchedulerBootstrap;
        MasterRPCServer closedRpcServer = masterRPCServer;
        MasterRegistryClient closedMasterRegistryClient = masterRegistryClient;
        // close spring Context and will invoke method with @PreDestroy annotation to destroy beans.
        // like ServerNodeManager,HostManager,TaskResponseService,CuratorZookeeperClient,etc
        SpringApplicationContext closedSpringContext = springApplicationContext) {
        logger.info("Master server is stopping, current cause : {}", cause);
    } catch (Exception e) {
        logger.error("MasterServer stop failed, current cause: {}", cause, e);
        return;
    }
    logger.info("MasterServer stopped, current cause: {}", cause);
}