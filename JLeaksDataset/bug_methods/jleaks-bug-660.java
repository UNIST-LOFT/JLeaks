    private Long2ObjectMap<Long2DoubleMap> buildItemRatings(LongSortedSet items) {
        final int nitems = items.size();

        // Create and initialize the transposed array to collect item vector data
        Long2ObjectMap<Long2DoubleMap> workMatrix =
                new Long2ObjectOpenHashMap<Long2DoubleMap>(nitems);
        LongIterator iter = items.iterator();
        while (iter.hasNext()) {
            long iid = iter.nextLong();
            workMatrix.put(iid, new Long2DoubleOpenHashMap(20));
        }

        LongCursor userCursor = dao.getUsers();
        while (userCursor.hasNext()) {
            long uid = userCursor.nextLong();
            SparseVector summary = userSummarizer.summarize(dao.getUserHistory(uid));
            MutableSparseVector normed = summary.mutableCopy();
            normalizer.normalize(uid, summary, normed);

            for (VectorEntry rating: normed.fast()) {
                final long item = rating.getKey();
                // get the item's rating vector
                Long2DoubleMap ivect = workMatrix.get(item);
                ivect.put(uid, rating.getValue());
            }
        }
        userCursor.close();

        return workMatrix;
    }
