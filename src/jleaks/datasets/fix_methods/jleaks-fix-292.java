public ExecuteResult execute(
    final ServiceContext serviceContext,
    final ConfiguredKsqlPlan ksqlPlan
) {
  try {
    final ExecuteResult result = EngineExecutor.create(
        engineContext,
        serviceContext,
        ksqlPlan.getConfig()
    ).execute(ksqlPlan.getPlan());

    // Having a streams running in a sandboxed environment is not necessary
    if (!getKsqlConfig().getBoolean(KsqlConfig.KSQL_SHARED_RUNTIME_ENABLED)) {
      result.getQuery().map(QueryMetadata::getKafkaStreams).ifPresent(streams -> streams.close());
    }
    return result;
  } finally {
    if (getKsqlConfig().getBoolean(KsqlConfig.KSQL_SHARED_RUNTIME_ENABLED)) {
      engineContext.getQueryRegistry().closeRuntimes();
    }
  }
}