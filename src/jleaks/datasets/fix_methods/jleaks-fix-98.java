public List<WorkUnit> getWorkunits(SourceState state) 
{
    Map<String, List<WorkUnit>> workUnits = Maps.newConcurrentMap();
    try {
        this.kafkaConsumerClient = kafkaConsumerClientResolver.resolveClass(state.getProp(GOBBLIN_KAFKA_CONSUMER_CLIENT_FACTORY_CLASS, DEFAULT_GOBBLIN_KAFKA_CONSUMER_CLIENT_FACTORY_CLASS)).newInstance().create(ConfigUtils.propertiesToConfig(state.getProperties()));
        List<KafkaTopic> topics = getFilteredTopics(state);
        for (KafkaTopic topic : topics) {
            LOG.info("Discovered topic " + topic.getName());
        }
        Map<String, State> topicSpecificStateMap = DatasetUtils.getDatasetSpecificProps(Iterables.transform(topics, new Function<KafkaTopic, String>() {

            @Override
            public String apply(KafkaTopic topic) {
                return topic.getName();
            }
        }), state);
        int numOfThreads = state.getPropAsInt(ConfigurationKeys.KAFKA_SOURCE_WORK_UNITS_CREATION_THREADS, ConfigurationKeys.KAFKA_SOURCE_WORK_UNITS_CREATION_DEFAULT_THREAD_COUNT);
        ExecutorService threadPool = Executors.newFixedThreadPool(numOfThreads, ExecutorsUtils.newThreadFactory(Optional.of(LOG)));
        Stopwatch createWorkUnitStopwatch = Stopwatch.createStarted();
        for (KafkaTopic topic : topics) {
            threadPool.submit(new WorkUnitCreator(topic, state, Optional.fromNullable(topicSpecificStateMap.get(topic.getName())), workUnits));
        }
        ExecutorsUtils.shutdownExecutorService(threadPool, Optional.of(LOG), 1L, TimeUnit.HOURS);
        LOG.info(String.format("Created workunits for %d topics in %d seconds", workUnits.size(), createWorkUnitStopwatch.elapsed(TimeUnit.SECONDS)));
        // Create empty WorkUnits for skipped partitions (i.e., partitions that have previous offsets,
        // but aren't processed).
        createEmptyWorkUnitsForSkippedPartitions(workUnits, topicSpecificStateMap, state);
        int numOfMultiWorkunits = state.getPropAsInt(ConfigurationKeys.MR_JOB_MAX_MAPPERS_KEY, ConfigurationKeys.DEFAULT_MR_JOB_MAX_MAPPERS);
        return KafkaWorkUnitPacker.getInstance(this, state).pack(workUnits, numOfMultiWorkunits);
    } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
        throw new RuntimeException(e);
    } finally {
        try {
            this.kafkaConsumerClient.close();
        } catch (IOException e) {
            throw new RuntimeException("Exception closing kafkaConsumerClient");
        }
    }
}