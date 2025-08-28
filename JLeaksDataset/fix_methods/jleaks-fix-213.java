
    public static List<SpanData> find(String traceId) {
        long blockIndex = BlockIndexEngine.newFinder().find(fetchStartTimeFromTraceId(traceId));
        if (blockIndex == 0) {
            return new ArrayList<SpanData>();
        }

        IndexDBConnector indexDBConnector = null;
        IndexMetaCollection indexMetaCollection = null;
        try {
            indexDBConnector = fetchIndexDBConnector(blockIndex);
            indexMetaCollection = indexDBConnector.queryByTraceId(traceId);
        } finally {
            if (indexDBConnector != null) {
                indexDBConnector.close();
            }
        }

        if (indexMetaCollection == null) {
            return new ArrayList<SpanData>();
        }

        Iterator<IndexMetaGroup<String>> iterator = IndexMetaCollections.group(indexMetaCollection, new GroupKeyBuilder<String>() {
            @Override
            public String buildKey(IndexMetaInfo metaInfo) {
                return metaInfo.getFileName();
            }
        }).iterator();

        List<SpanData> result = new ArrayList<SpanData>();
        while (iterator.hasNext()) {
            IndexMetaGroup<String> group = iterator.next();
            DataFileReader reader = null;
            try {
                reader = new DataFileReader(group.getKey());
                result.addAll(reader.read(group.getMetaInfo()));
            } finally {
                if (reader != null) {
                    reader.close();
                }
            }
        }

        return result;
    }
