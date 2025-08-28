public void analyze(List<Aggregation> registeredAggregations, JVMEventChannel eventBus, DataSourceChannel dataSourceBus) 
{
    Phaser finishLine = new Phaser();
    Set<EventSource> generatedEvents = diary.generatesEvents();
    for (Aggregation aggregation : registeredAggregations) {
        if (debugging)
            LOGGER.log(Level.INFO, "Evaluating: " + aggregation.toString());
        Constructor<? extends Aggregator<?>> constructor = constructor(aggregation);
        if (constructor == null) {
            LOGGER.log(Level.WARNING, "Cannot find one of: default constructor or @Collates annotation for " + aggregation.getClass().getName());
            continue;
        }
        if (debugging)
            LOGGER.log(Level.INFO, "Loading   : " + aggregation.toString());
        Aggregator<? extends Aggregation> aggregator = null;
        try {
            aggregator = constructor.newInstance(aggregation);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            continue;
        }
        aggregatedData.put(aggregation.getClass(), aggregation);
        Optional<EventSource> source = generatedEvents.stream().filter(aggregator::aggregates).findFirst();
        if (source.isPresent()) {
            LOGGER.log(Level.FINE, "Registering: " + aggregation.getClass().getName());
            finishLine.register();
            aggregator.onCompletion(finishLine::arriveAndDeregister);
            JVMEventChannelAggregator eventChannelAggregator = new JVMEventChannelAggregator(source.get().toChannel(), aggregator);
            eventBus.registerListener(eventChannelAggregator);
        }
    }
    try {
        if (finishLine.getRegisteredParties() > 0) {
            dataSource.stream().forEach(message -> dataSourceBus.publish(ChannelName.DATA_SOURCE, message));
            finishLine.awaitAdvance(0);
        } else {
            LOGGER.log(Level.INFO, "No Aggregations have been registered, DataSource will not be analysed.");
            LOGGER.log(Level.INFO, "Is there a module containing Aggregation classes on the module-path");
            LOGGER.log(Level.INFO, "Is GCToolKit::loadAggregationsFromServiceLoader() or GCToolKit::loadAggregation(Aggregation) being invoked?");
        }
        // Fill in termination info.
        Optional<Aggregation> aggregation = aggregatedData.values().stream().findFirst();
        aggregation.ifPresent(terminationRecord -> {
            setJVMTerminationTime(terminationRecord.timeOfTerminationEvent());
            setRuntimeDuration(terminationRecord.estimatedRuntime());
            setEstimatedJVMStartTime(terminationRecord.estimatedStartTime());
        });
    } catch (IOException ioe) {
        LOGGER.log(Level.SEVERE, ioe.getMessage(), ioe);
    } finally {
        dataSourceBus.close();
        eventBus.close();
    }
}