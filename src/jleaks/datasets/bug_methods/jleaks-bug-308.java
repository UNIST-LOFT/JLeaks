    public void addContext(int jobSearchContextId, JobQueryShardContext shardQueryContext) {
        interruptIfKilled();
        if (closed.get()) {
            throw new IllegalStateException("context already closed");
        }
        synchronized (subContextLock) {
            if (queryContexts.put(jobSearchContextId, shardQueryContext) != null) {
                throw new IllegalArgumentException(String.format(Locale.ENGLISH,
                        "ExecutionSubContext for %d already added", jobSearchContextId));
            }
        }
        int numActive = activeQueryContexts.incrementAndGet();
        LOGGER.trace("adding query subContext {}, now there are {} query subContexts", jobSearchContextId, numActive);

        shardQueryContext.addCallback(new RemoveQueryContextCallback(jobSearchContextId));
        EngineSearcherDelegate searcherDelegate = acquireSearcher(shardQueryContext.indexShard());
        shardQueryContext.searcher(searcherDelegate);

        if (collectNode.keepContextForFetcher()) {
            JobFetchShardContext shardFetchContext = new JobFetchShardContext(
                    searcherDelegate,
                    shardQueryContext.searchContext());
            synchronized (subContextLock) {
                fetchContexts.put(jobSearchContextId, shardFetchContext);
            }
            shardFetchContext.addCallback(new RemoveFetchContextCallback(jobSearchContextId));

            int numActiveFetch = activeFetchContexts.incrementAndGet();
            LOGGER.trace("adding fetch subContext {}, now there are {} fetch subContexts", jobSearchContextId, numActiveFetch);
        }
    }
