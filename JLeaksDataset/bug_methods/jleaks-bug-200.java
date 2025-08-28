  private void buildStarTree() throws Exception {
    // Create stats collector
    StatsCollectorConfig statsCollectorConfig = new StatsCollectorConfig(dataSchema, config.getSegmentPartitionConfig());
    SegmentPreIndexStatsCollectorImpl statsCollector = new SegmentPreIndexStatsCollectorImpl(statsCollectorConfig);
    statsCollector.init();
    segmentStats = statsCollector;

    long start = System.currentTimeMillis();
    //construct star tree builder config
    StarTreeIndexSpec starTreeIndexSpec = config.getStarTreeIndexSpec();
    if (starTreeIndexSpec == null) {
      starTreeIndexSpec = new StarTreeIndexSpec();
      starTreeIndexSpec.setMaxLeafRecords(StarTreeIndexSpec.DEFAULT_MAX_LEAF_RECORDS);
      config.setStarTreeIndexSpec(starTreeIndexSpec);
    }
    List<String> dimensionsSplitOrder = starTreeIndexSpec.getDimensionsSplitOrder();
    if (dimensionsSplitOrder != null && !dimensionsSplitOrder.isEmpty()) {
      final String timeColumnName = config.getTimeColumnName();
      if (timeColumnName != null) {
        dimensionsSplitOrder.remove(timeColumnName);
      }
    }
    //create star builder config from startreeindexspec. Merge these two in one later.
    StarTreeBuilderConfig starTreeBuilderConfig = new StarTreeBuilderConfig();
    starTreeBuilderConfig.setSchema(dataSchema);
    starTreeBuilderConfig.setDimensionsSplitOrder(dimensionsSplitOrder);
    starTreeBuilderConfig.setMaxLeafRecords(starTreeIndexSpec.getMaxLeafRecords());
    starTreeBuilderConfig.setSkipStarNodeCreationForDimensions(
        starTreeIndexSpec.getSkipStarNodeCreationForDimensions());
    Set<String> skipMaterializationForDimensions = starTreeIndexSpec.getskipMaterializationForDimensions();
    starTreeBuilderConfig.setSkipMaterializationForDimensions(skipMaterializationForDimensions);
    starTreeBuilderConfig.setSkipMaterializationCardinalityThreshold(
        starTreeIndexSpec.getskipMaterializationCardinalityThreshold());
    starTreeBuilderConfig.setOutDir(starTreeTempDir);

    boolean enableOffHeapFormat = starTreeIndexSpec.isEnableOffHeapFormat();
    starTreeBuilderConfig.setEnableOffHealpFormat(enableOffHeapFormat);

    //initialize star tree builder
    StarTreeBuilder starTreeBuilder = new OffHeapStarTreeBuilder();
    starTreeBuilder.init(starTreeBuilderConfig);
    //build star tree along with collecting stats
    recordReader.rewind();
    LOGGER.info("Start append raw data to star tree builder!");
    totalDocs = 0;
    GenericRow readRow = new GenericRow();
    GenericRow transformedRow = new GenericRow();
    while (recordReader.hasNext()) {
      //PlainFieldExtractor conducts necessary type conversions
      transformedRow = readNextRowSanitized(readRow, transformedRow);
      //must be called after previous step since type conversion for derived values is unnecessary
      populateDefaultDerivedColumnValues(transformedRow);
      starTreeBuilder.append(transformedRow);
      statsCollector.collectRow(transformedRow);
      totalRawDocs++;
      totalDocs++;
    }
    recordReader.close();
    LOGGER.info("Start building star tree!");
    starTreeBuilder.build();
    LOGGER.info("Finished building star tree!");
    long starTreeBuildFinishTime = System.currentTimeMillis();
    //build stats
    // Count the number of documents and gather per-column statistics
    LOGGER.info("Start building StatsCollector!");
    Iterator<GenericRow> aggregatedRowsIterator = starTreeBuilder.iterator(starTreeBuilder.getTotalRawDocumentCount(),
        starTreeBuilder.getTotalRawDocumentCount() + starTreeBuilder.getTotalAggregateDocumentCount());
    while (aggregatedRowsIterator.hasNext()) {
      GenericRow genericRow = aggregatedRowsIterator.next();
      statsCollector.collectRow(genericRow, true /* isAggregated */);
      totalAggDocs++;
      totalDocs++;
    }
    statsCollector.build();
    buildIndexCreationInfo();
    LOGGER.info("Collected stats for {} raw documents, {} aggregated documents", totalRawDocs, totalAggDocs);
    long statCollectionFinishTime = System.currentTimeMillis();
    // Initialize the index creation using the per-column statistics information
    indexCreator.init(config, segmentIndexCreationInfo, indexCreationInfoMap, dataSchema, tempIndexDir);

    //iterate over the data again,
    Iterator<GenericRow> allRowsIterator = starTreeBuilder.iterator(0,
        starTreeBuilder.getTotalRawDocumentCount() + starTreeBuilder.getTotalAggregateDocumentCount());

    while (allRowsIterator.hasNext()) {
      GenericRow genericRow = allRowsIterator.next();
      indexCreator.indexRow(genericRow);
    }

    // If no dimensionsSplitOrder was specified in starTreeIndexSpec, set the order used by the starTreeBuilder.
    // This is required so the dimensionsSplitOrder used by the builder can be written into the segment metadata.
    if (dimensionsSplitOrder == null || dimensionsSplitOrder.isEmpty()) {
      starTreeIndexSpec.setDimensionsSplitOrder(starTreeBuilder.getDimensionsSplitOrder());
    }

    if (skipMaterializationForDimensions == null || skipMaterializationForDimensions.isEmpty()) {
      starTreeIndexSpec.setSkipMaterializationForDimensions(starTreeBuilder.getSkipMaterializationForDimensions());
    }

    serializeTree(starTreeBuilder, enableOffHeapFormat);
    //post creation
    handlePostCreation();
    starTreeBuilder.cleanup();
    long end = System.currentTimeMillis();
    LOGGER.info("Total time:{} \n star tree build time:{} \n stat collection time:{} \n column index build time:{}",
        (end - start), (starTreeBuildFinishTime - start), statCollectionFinishTime - starTreeBuildFinishTime,
        end - statCollectionFinishTime);
  }
