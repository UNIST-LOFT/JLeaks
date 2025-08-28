    public InternalEngine(EngineConfig engineConfig) throws EngineException {
        Preconditions.checkNotNull(engineConfig.getStore(), "Store must be provided to the engine");
        Preconditions.checkNotNull(engineConfig.getDeletionPolicy(), "Snapshot deletion policy must be provided to the engine");
        Preconditions.checkNotNull(engineConfig.getTranslog(), "Translog must be provided to the engine");

        this.shardId = engineConfig.getShardId();
        this.logger = Loggers.getLogger(getClass(), engineConfig.getIndexSettings(), shardId);
        this.lastDeleteVersionPruneTimeMSec = engineConfig.getThreadPool().estimatedTimeInMillis();
        this.indexingService = engineConfig.getIndexingService();
        this.warmer = engineConfig.getWarmer();
        this.store = engineConfig.getStore();
        this.deletionPolicy = engineConfig.getDeletionPolicy();
        this.translog = engineConfig.getTranslog();
        this.mergePolicyProvider = engineConfig.getMergePolicyProvider();
        this.mergeScheduler = engineConfig.getMergeScheduler();
        this.versionMap = new LiveVersionMap();
        this.dirtyLocks = new Object[engineConfig.getIndexConcurrency() * 50]; // we multiply it to have enough...
        for (int i = 0; i < dirtyLocks.length; i++) {
            dirtyLocks[i] = new Object();
        }

        this.mergeSchedulerFailureListener = new FailEngineOnMergeFailure();
        this.mergeSchedulerListener = new MergeSchedulerListener();
        this.mergeScheduler.addListener(mergeSchedulerListener);
        this.mergeScheduler.addFailureListener(mergeSchedulerFailureListener);

        this.failedEngineListener = engineConfig.getFailedEngineListener();
        throttle = new IndexThrottle();
        this.engineConfig = engineConfig;
        listener = new EngineConfig.EngineSettingsListener(logger, engineConfig) {
            @Override
            protected void onChange() {
                updateSettings();
            }
        };
        engineConfig.getIndexSettingsService().addListener(listener);
        final IndexWriter writer = start();
        assert indexWriter == null : "IndexWriter already initialized";
        indexWriter = writer;
    }
