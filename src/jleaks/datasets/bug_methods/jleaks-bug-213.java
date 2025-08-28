    public static List<SpanData> find(String traceId) {
        long blockIndex = BlockIndexEngine.newFinder().find(fetchStartTimeFromTraceId(traceId));
        if (blockIndex == 0) {
            return new ArrayList<SpanData>();
        }

        IndexDBConnector indexDBConnector = fetchIndexDBConnector(blockIndex);
        IndexMetaCollection indexMetaCollection = indexDBConnector.queryByTraceId(traceId);
        indexDBConnector.close();

        Iterator<IndexMetaGroup<String>> iterator =
                IndexMetaCollections.group(indexMetaCollection, new GroupKeyBuilder<String>() {
                    @Override
                    public String buildKey(IndexMetaInfo metaInfo) {
                        return metaInfo.getFileName();
                    }
                }).iterator();

        List<SpanData> result = new ArrayList<SpanData>();
        while (iterator.hasNext()) {
            IndexMetaGroup<String> group = iterator.next();
            result.addAll(new DataFileReader(group.getKey()).read(group.getMetaInfo()));
        }

        return result;
    }
