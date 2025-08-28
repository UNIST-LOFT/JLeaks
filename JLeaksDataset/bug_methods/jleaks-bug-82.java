    public void close(String cause) {

        try {
            // set stop signal is true
            // execute only once
            if (!Stopper.stop()) {
                logger.warn("MasterServer is already stopped, current cause: {}", cause);
                return;
            }

            logger.info("Master server is stopping, current cause : {}", cause);

            // thread sleep 3 seconds for thread quietly stop
            ThreadUtils.sleep(Constants.SERVER_CLOSE_WAIT_TIME.toMillis());
            // close
            this.schedulerApi.close();
            this.masterSchedulerBootstrap.close();
            this.masterRPCServer.close();
            this.masterRegistryClient.closeRegistry();
            // close spring Context and will invoke method with @PreDestroy annotation to destroy beans.
            // like ServerNodeManager,HostManager,TaskResponseService,CuratorZookeeperClient,etc
            springApplicationContext.close();

            logger.info("MasterServer stopped, current cause: {}", cause);
        } catch (Exception e) {
            logger.error("MasterServer stop failed, current cause: {}", cause, e);
        }
    }
