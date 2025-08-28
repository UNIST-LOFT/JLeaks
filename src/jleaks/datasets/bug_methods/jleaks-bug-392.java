    private GetResult innerGet(
        String id,
        String[] gFields,
        boolean realtime,
        long version,
        VersionType versionType,
        long ifSeqNo,
        long ifPrimaryTerm,
        FetchSourceContext fetchSourceContext,
        boolean forceSyntheticSource
    ) throws IOException {
        fetchSourceContext = normalizeFetchSourceContent(fetchSourceContext, gFields);

        Engine.GetResult get = indexShard.get(
            new Engine.Get(realtime, realtime, id).version(version)
                .versionType(versionType)
                .setIfSeqNo(ifSeqNo)
                .setIfPrimaryTerm(ifPrimaryTerm)
        );
        if (get.exists() == false) {
            get.close();
        }

        if (get == null || get.exists() == false) {
            return new GetResult(shardId.getIndexName(), id, UNASSIGNED_SEQ_NO, UNASSIGNED_PRIMARY_TERM, -1, false, null, null, null);
        }

        try {
            // break between having loaded it from translog (so we only have _source), and having a document to load
            return innerGetFetch(id, gFields, fetchSourceContext, get, forceSyntheticSource);
        } finally {
            get.close();
        }
    }
