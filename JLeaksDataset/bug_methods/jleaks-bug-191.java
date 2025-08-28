    private PTable buildTable(byte[] key, ImmutableBytesPtr cacheKey, Region region,
            long clientTimeStamp) throws IOException, SQLException {
        Scan scan = MetaDataUtil.newTableRowsScan(key, MIN_TABLE_TIMESTAMP, clientTimeStamp);
        RegionScanner scanner = region.getScanner(scan);

        Cache<ImmutableBytesPtr,PMetaDataEntity> metaDataCache = GlobalCache.getInstance(this.env).getMetaDataCache();
        try {
            PTable oldTable = (PTable)metaDataCache.getIfPresent(cacheKey);
            long tableTimeStamp = oldTable == null ? MIN_TABLE_TIMESTAMP-1 : oldTable.getTimeStamp();
            PTable newTable;
            boolean blockWriteRebuildIndex = env.getConfiguration().getBoolean(QueryServices.INDEX_FAILURE_BLOCK_WRITE, 
                    QueryServicesOptions.DEFAULT_INDEX_FAILURE_BLOCK_WRITE);
            newTable = getTable(scanner, clientTimeStamp, tableTimeStamp);
            if (newTable == null) {
                return null;
            }
            if (oldTable == null || tableTimeStamp < newTable.getTimeStamp()
                    || (blockWriteRebuildIndex && newTable.getIndexDisableTimestamp() > 0)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Caching table "
                            + Bytes.toStringBinary(cacheKey.get(), cacheKey.getOffset(),
                                cacheKey.getLength()) + " at seqNum "
                            + newTable.getSequenceNumber() + " with newer timestamp "
                            + newTable.getTimeStamp() + " versus " + tableTimeStamp);
                }
                metaDataCache.put(cacheKey, newTable);
            }
            return newTable;
        } finally {
            scanner.close();
        }
    }
