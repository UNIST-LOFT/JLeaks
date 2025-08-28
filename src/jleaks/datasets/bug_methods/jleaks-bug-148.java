    public StandardHugeGraph(HugeConfig config) {
        this.params = new StandardHugeGraphParams();
        this.configuration = config;

        this.schemaEventHub = new EventHub("schema");
        this.graphEventHub = new EventHub("graph");
        this.indexEventHub = new EventHub("index");

        final int writeLimit = config.get(CoreOptions.RATE_LIMIT_WRITE);
        this.writeRateLimiter = writeLimit > 0 ?
                                RateLimiter.create(writeLimit) : null;
        final int readLimit = config.get(CoreOptions.RATE_LIMIT_READ);
        this.readRateLimiter = readLimit > 0 ?
                               RateLimiter.create(readLimit) : null;

        boolean ramtableEnable = config.get(CoreOptions.QUERY_RAMTABLE_ENABLE);
        if (ramtableEnable) {
            long vc = config.get(CoreOptions.QUERY_RAMTABLE_VERTICES_CAPACITY);
            int ec = config.get(CoreOptions.QUERY_RAMTABLE_EDGES_CAPACITY);
            this.ramtable = new RamTable(this, vc, ec);
        } else {
            this.ramtable = null;
        }

        this.taskManager = TaskManager.instance();

        this.features = new HugeFeatures(this, true);

        this.name = config.get(CoreOptions.STORE);
        this.started = false;
        this.closed = false;
        this.mode = GraphMode.NONE;
        this.readMode = GraphReadMode.OLTP_ONLY;

        LockUtil.init(this.name);

        try {
            this.storeProvider = this.loadStoreProvider();
        } catch (BackendException e) {
            LockUtil.destroy(this.name);
            String message = "Failed to load backend store provider";
            LOG.error("{}: {}", message, e.getMessage());
            throw new HugeException(message);
        }

        this.tx = new TinkerPopTransaction(this);

        SnowflakeIdGenerator.init(this.params);

        this.taskManager.addScheduler(this.params);
        this.authManager = new StandardAuthManager(this.params);
        this.variables = null;
    }
