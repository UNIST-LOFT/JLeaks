public <T extends ControllerEvent> EventProcessorGroup<T> createEventProcessorGroup(
    final EventProcessorConfig<T> eventProcessorConfig,
    final CheckpointStore checkpointStore, final ScheduledExecutorService rebalanceExecutor) throws CheckpointStoreException {
        Preconditions.checkNotNull(eventProcessorConfig, "eventProcessorConfig");
        Preconditions.checkNotNull(checkpointStore, "checkpointStore");

        EventProcessorGroupImpl<T> actorGroup;

        // Create event processor group.
        actorGroup = new EventProcessorGroupImpl<>(this, eventProcessorConfig, checkpointStore, rebalanceExecutor);
        try {
            // Initialize it.
            actorGroup.initialize();

            actorGroup.startAsync();
        } catch (Throwable ex) {
            actorGroup.close();
            throw ex;
        }

        return actorGroup;
    }