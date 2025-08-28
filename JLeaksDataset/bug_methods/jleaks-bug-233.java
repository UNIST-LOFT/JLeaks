    public <Q, R> Publisher<QueryResponseMessage<R>> streamingQuery(StreamingQueryMessage<Q, R> query) {
        Span span = spanFactory.createDispatchSpan(() -> "AxonServerQueryBus.streamingQuery", query).start();
        SpanScope scope = span.makeCurrent();
        StreamingQueryMessage<Q, R> queryWithContext = spanFactory.propagateContext(query);
        int priority = priorityCalculator.determinePriority(queryWithContext);
        AtomicReference<Scheduler> scheduler = new AtomicReference<>(PriorityTaskSchedulers.forPriority(
                queryExecutor,
                priority,
                TASK_SEQUENCE));
        scope.close();
        return Mono.fromSupplier(this::registerStreamingQueryActivity).flatMapMany(
                activity -> Mono.just(dispatchInterceptors.intercept(queryWithContext))
                                .flatMapMany(intercepted ->
                                                     Mono.just(serializeStreaming(intercepted, priority))
                                                         .flatMapMany(queryRequest -> new ResultStreamPublisher<>(
                                                                 () -> sendRequest(intercepted, queryRequest)))
                                                         .concatMap(queryResponse -> deserialize(intercepted,
                                                                                                 queryResponse))
                                )
                                .publishOn(scheduler.get())
                                .doOnError(span::recordException)
                                .doFinally(new ActivityFinisher(activity, span))
                                .subscribeOn(scheduler.get()));
    }
