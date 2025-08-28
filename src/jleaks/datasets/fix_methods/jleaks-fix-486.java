
    public Node(FactoryImpl factory, Config config) {
        ThreadContext.get().setCurrentFactory(factory);
        this.threadGroup = new ThreadGroup(factory.getName());
        this.factory = factory;
        this.config = config;
        this.groupProperties = new GroupProperties(config);
        this.liteMember = config.isLiteMember();
        this.localNodeType = (liteMember) ? NodeType.LITE_MEMBER : NodeType.MEMBER;
        systemLogService = new SystemLogService(this);
        ServerSocketChannel serverSocketChannel = null;
        Address localAddress = null;
        try {
            AddressPicker addressPicker = new AddressPicker(this);
            addressPicker.pickAddress();
            localAddress = addressPicker.getAddress();
            serverSocketChannel = addressPicker.getServerSocketChannel();
        } catch (Throwable e) {
            Util.throwUncheckedException(e);
        }
        address = localAddress;
        localMember = new MemberImpl(address, true, localNodeType, UUID.randomUUID().toString());
        String loggingType = groupProperties.LOGGING_TYPE.getString();
        loggingService = new LoggingServiceImpl(systemLogService, config.getGroupConfig().getName(), loggingType, localMember);
        logger = loggingService.getLogger(Node.class.getName());
        initializer = NodeInitializerFactory.create();
        try {
            initializer.beforeInitialize(this);
        } catch (Throwable e) {
            try {
                serverSocketChannel.close();
            } catch (Throwable ignored) {}
            Util.throwUncheckedException(e);
        }
        securityContext = config.getSecurityConfig().isEnabled() ? initializer.getSecurityContext() : null;
        clusterImpl = new ClusterImpl(this);
        baseVariables = new NodeBaseVariables(address, localMember);
        //initialize managers..
        clusterService = new ClusterService(this);
        clusterService.start();
        connectionManager = new ConnectionManager(new NodeIOService(this), serverSocketChannel);
        clusterManager = new ClusterManager(this);
        executorManager = new ExecutorManager(this);
        clientHandlerService = new ClientHandlerService(this);
        concurrentMapManager = new ConcurrentMapManager(this);
        blockingQueueManager = new BlockingQueueManager(this);
        listenerManager = new ListenerManager(this);
        clientService = new ClientServiceImpl(concurrentMapManager);
        topicManager = new TopicManager(this);
        textCommandService = new TextCommandServiceImpl(this);
        clusterManager.addMember(false, localMember);
        initializer.printNodeInfo(this);
        buildNumber = initializer.getBuildNumber();
        VersionCheck.check(this, initializer.getBuild(), initializer.getVersion());
        Join join = config.getNetworkConfig().getJoin();
        MulticastService mcService = null;
        try {
            if (join.getMulticastConfig().isEnabled()) {
                MulticastConfig multicastConfig = join.getMulticastConfig();
                MulticastSocket multicastSocket = new MulticastSocket(null);
                multicastSocket.setReuseAddress(true);
                // bind to receive interface
                multicastSocket.bind(new InetSocketAddress(multicastConfig.getMulticastPort()));
                multicastSocket.setTimeToLive(multicastConfig.getMulticastTimeToLive());
                // set the send interface
                try {
                    multicastSocket.setInterface(address.getInetAddress());
                } catch (Exception e) {
                    logger.log(Level.WARNING, e.getMessage(), e);
                }
                multicastSocket.setReceiveBufferSize(64 * 1024);
                multicastSocket.setSendBufferSize(64 * 1024);
                String multicastGroup = System.getProperty("hazelcast.multicast.group");
                if (multicastGroup == null) {
                    multicastGroup = multicastConfig.getMulticastGroup();
                }
                multicastConfig.setMulticastGroup(multicastGroup);
                multicastSocket.joinGroup(InetAddress.getByName(multicastGroup));
                multicastSocket.setSoTimeout(1000);
                mcService = new MulticastService(this, multicastSocket);
                mcService.addMulticastListener(new NodeMulticastListener(this));
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        this.multicastService = mcService;
        wanReplicationService = new WanReplicationService(this);
        initializeListeners(config);
        joiner = createJoiner();
    }