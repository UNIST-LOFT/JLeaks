  public static Sequence<ResultRow> process(
      final GroupByQuery query,
      @Nullable final StorageAdapter storageAdapter,
      final NonBlockingPool<ByteBuffer> intermediateResultsBufferPool,
      final GroupByQueryConfig querySpecificConfig,
      final QueryConfig queryConfig
  )
  {
    if (storageAdapter == null) {
      throw new ISE(
          "Null storage adapter found. Probably trying to issue a query against a segment being memory unmapped."
      );
    }

    final List<Interval> intervals = query.getQuerySegmentSpec().getIntervals();
    if (intervals.size() != 1) {
      throw new IAE("Should only have one interval, got[%s]", intervals);
    }

    final ResourceHolder<ByteBuffer> bufferHolder = intermediateResultsBufferPool.take();

    final String fudgeTimestampString = NullHandling.emptyToNullIfNeeded(
        query.getContextValue(GroupByStrategyV2.CTX_KEY_FUDGE_TIMESTAMP, null)
    );

    final DateTime fudgeTimestamp = fudgeTimestampString == null
                                    ? null
                                    : DateTimes.utc(Long.parseLong(fudgeTimestampString));

    final Filter filter = Filters.convertToCNFFromQueryContext(query, Filters.toFilter(query.getFilter()));
    final Interval interval = Iterables.getOnlyElement(query.getIntervals());

    final boolean doVectorize = queryConfig.getVectorize().shouldVectorize(
        VectorGroupByEngine.canVectorize(query, storageAdapter, filter)
    );

    final Sequence<ResultRow> result;

    if (doVectorize) {
      result = VectorGroupByEngine.process(
          query,
          storageAdapter,
          bufferHolder.get(),
          fudgeTimestamp,
          filter,
          interval,
          querySpecificConfig,
          queryConfig
      );
    } else {
      result = processNonVectorized(
          query,
          storageAdapter,
          bufferHolder.get(),
          fudgeTimestamp,
          querySpecificConfig,
          filter,
          interval
      );
    }

    return result.withBaggage(bufferHolder);
  }
