public synchronized void start() throws Exception 
{
    if (isRunning()) {
        return;
    }
    try {
        ServerConfig serverConfig;
        LOGGER.info("Starting server...");
        DefinitionBuild definitionBuild = buildUserDefinition();
        if (definitionBuild.error != null) {
            if (definitionBuild.getServerConfig().isDevelopment()) {
                LOGGER.warn("Exception raised getting server config (will use default config until reload):", definitionBuild.error);
                needsReload.set(true);
            } else {
                throw Exceptions.toException(definitionBuild.error);
            }
        }
        serverConfig = definitionBuild.getServerConfig();
        execController = new DefaultExecController(serverConfig.getThreads());
        ChannelHandler channelHandler = ExecThreadBinding.bindFor(true, execController, () -> buildHandler(definitionBuild));
        channel = buildChannel(serverConfig, channelHandler);
        boundAddress = (InetSocketAddress) channel.localAddress();
        String startMessage = String.format("Ratpack started %sfor %s://%s:%s", serverConfig.isDevelopment() ? "(development) " : "", getScheme(), getBindHost(), getBindPort());
        if (serverConfig.getPortFile().isPresent()) {
            final Path portFilePath = serverConfig.getPortFile().get();
            try (FileOutputStream fout = new FileOutputStream(portFilePath.toFile())) {
                fout.write(Integer.toString(getBindPort()).getBytes());
            }
        }
        if (Slf4jNoBindingDetector.isHasBinding()) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(startMessage);
            }
        } else {
            System.out.println(startMessage);
        }
        if (serverConfig.isRegisterShutdownHook()) {
            shutdownHookThread = new Thread("ratpack-shutdown-thread") {

                @Override
                public void run() {
                    try {
                        DefaultRatpackServer.this.stop();
                    } catch (Exception ignored) {
                        ignored.printStackTrace(System.err);
                    }
                }
            };
            Runtime.getRuntime().addShutdownHook(shutdownHookThread);
        }
    } catch (Exception e) {
        if (execController != null) {
            execController.close();
        }
        stop();
        throw e;
    }
}