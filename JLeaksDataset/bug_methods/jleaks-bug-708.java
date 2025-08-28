    public void analyze(List<Aggregation> registeredAggregations, JVMEventChannel eventBus, DataSourceChannel dataSourceBus) {
        Phaser finishLine = new Phaser();
        try {
            Set<EventSource> generatedEvents = diary.generatesEvents();
            for (Aggregation aggregation : registeredAggregations) {
                Constructor<? extends Aggregator<?>> constructor = constructor(aggregation);
                if ( constructor == null) {
                    LOGGER.log(Level.WARNING, "Cannot find one of: default constructor or @Collates annotation for " + aggregation.getClass().getName());
                    continue;
                }
                Aggregator<? extends Aggregation> aggregator = constructor.newInstance(aggregation);
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

            if ( finishLine.getRegisteredParties() > 0) {
                dataSource.stream().forEach(message -> dataSourceBus.publish(ChannelName.DATA_SOURCE, message));
                finishLine.awaitAdvance(0);
            } else {
                LOGGER.log(Level.INFO, "No Aggregations have been registered, DataSource will not be analysed.");
                LOGGER.log(Level.INFO, "Is there a module containing Aggregation classes on the module-path");
                LOGGER.log(Level.INFO, "Is GCToolKit::loadAggregationsFromServiceLoader() or GCToolKit::loadAggregation(Aggregation) being invoked?");
            }
            dataSourceBus.close();
            eventBus.close();

            // Fill in termination info.
            Optional<Aggregation> aggregation = aggregatedData.values().stream().findFirst();
            aggregation.ifPresent(terminationRecord -> {
                setJVMTerminationTime(terminationRecord.timeOfTerminationEvent());
                setRuntimeDuration(terminationRecord.estimatedRuntime());
                setEstimatedJVMStartTime(terminationRecord.estimatedStartTime());
            });
        } catch (IOException | ClassCastException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
        }
    }
