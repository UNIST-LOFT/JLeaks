private void closeShardInjector(String reason, ShardId sId, Injector shardInjector, IndexShard indexShard) 
{
    final int shardId = sId.id();
    try {
        try {
            indicesLifecycle.beforeIndexShardClosed(sId, indexShard, indexSettings);
        } finally {
            // close everything else even if the beforeIndexShardClosed threw an exception
            for (Class<? extends Closeable> closeable : pluginsService.shardServices()) {
                try {
                    shardInjector.getInstance(closeable).close();
                } catch (Throwable e) {
                    logger.debug("[{}] failed to clean plugin shard service [{}]", e, shardId, closeable);
                }
            }
            // now we can close the translog service, we need to close it before the we close the shard
            closeInjectorResource(sId, shardInjector, TranslogService.class);
            // this logic is tricky, we want to close the engine so we rollback the changes done to it
            // and close the shard so no operations are allowed to it
            if (indexShard != null) {
                try {
                    // only flush we are we closed (closed index or shutdown) and if we are not deleted
                    final boolean flushEngine = deleted.get() == false && closed.get();
                    indexShard.close(reason, flushEngine);
                } catch (Throwable e) {
                    logger.debug("[{}] failed to close index shard", e, shardId);
                    // ignore
                }
            }
            closeInjectorResource(sId, shardInjector, MergeSchedulerProvider.class, MergePolicyProvider.class, IndexShardGatewayService.class, Translog.class, PercolatorQueriesRegistry.class);
            // call this before we close the store, so we can release resources for it
            indicesLifecycle.afterIndexShardClosed(sId, indexShard, indexSettings);
        }
    } finally {
        try {
            shardInjector.getInstance(Store.class).close();
        } catch (Throwable e) {
            logger.warn("[{}] failed to close store on shard removal (reason: [{}])", e, shardId, reason);
        }
    }
}