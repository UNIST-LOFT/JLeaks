    public int submitJob(final Configuration conf, final String qualifiedTableName,
        final String inputPaths, final Path outputPath, List<TargetTableRef> tablesToBeLoaded, boolean hasLocalIndexes) throws Exception {
       
        Job job = Job.getInstance(conf, "Phoenix MapReduce import for " + qualifiedTableName);
        FileInputFormat.addInputPaths(job, inputPaths);
        FileOutputFormat.setOutputPath(job, outputPath);

        job.setInputFormatClass(PhoenixTextInputFormat.class);
        job.setMapOutputKeyClass(TableRowkeyPair.class);
        job.setMapOutputValueClass(ImmutableBytesWritable.class);
        job.setOutputKeyClass(TableRowkeyPair.class);
        job.setOutputValueClass(KeyValue.class);
        job.setReducerClass(FormatToKeyValueReducer.class);
        byte[][] splitKeysBeforeJob = null;
        org.apache.hadoop.hbase.client.Connection hbaseConn =
                ConnectionFactory.createConnection(job.getConfiguration());
        RegionLocator regionLocator = null;
        if(hasLocalIndexes) {
            try{
                regionLocator = hbaseConn.getRegionLocator(TableName.valueOf(qualifiedTableName));
                splitKeysBeforeJob = regionLocator.getStartKeys();
            } finally {
                if(regionLocator != null )regionLocator.close();
            }
        }
        MultiHfileOutputFormat.configureIncrementalLoad(job, tablesToBeLoaded);

        final String tableNamesAsJson = TargetTableRefFunctions.NAMES_TO_JSON.apply(tablesToBeLoaded);
        final String logicalNamesAsJson = TargetTableRefFunctions.LOGICAL_NAMES_TO_JSON.apply(tablesToBeLoaded);

        job.getConfiguration().set(FormatToBytesWritableMapper.TABLE_NAMES_CONFKEY,tableNamesAsJson);
        job.getConfiguration().set(FormatToBytesWritableMapper.LOGICAL_NAMES_CONFKEY,logicalNamesAsJson);

        // give subclasses their hook
        setupJob(job);

        LOG.info("Running MapReduce import job from {} to {}", inputPaths, outputPath);
        boolean success = job.waitForCompletion(true);

        if (success) {
            if (hasLocalIndexes) {
                try {
                    regionLocator = hbaseConn.getRegionLocator(TableName.valueOf(qualifiedTableName));
                    if(!IndexUtil.matchingSplitKeys(splitKeysBeforeJob, regionLocator.getStartKeys())) {
                        LOG.error("The table "
                                + qualifiedTableName
                                + " has local indexes and there is split key mismatch before and"
                                + " after running bulkload job. Please rerun the job otherwise"
                                + " there may be inconsistencies between actual data and index data.");
                        return -1;
                    }
                } finally {
                    if (regionLocator != null) regionLocator.close();
                }
            }
            LOG.info("Loading HFiles from {}", outputPath);
            completebulkload(conf,outputPath,tablesToBeLoaded);
            LOG.info("Removing output directory {}", outputPath);
            if(!outputPath.getFileSystem(conf).delete(outputPath, true)) {
                LOG.error("Failed to delete the output directory {}", outputPath);
            }
            return 0;
        } else {
            return -1;
        }
    }
