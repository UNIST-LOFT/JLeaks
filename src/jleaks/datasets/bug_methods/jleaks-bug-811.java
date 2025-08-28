    public BatchingVisitable<SortedMap<Long, Multimap<TableReference, Cell>>> getBatchingVisitableScrubQueue(
            int cellsToScrubBatchSize,
            long maxScrubTimestamp /* exclusive */,
            byte[] startRow,
            byte[] endRow) {
        ClosableIterator<RowResult<Value>> iterator =
                getIteratorToScrub(cellsToScrubBatchSize, maxScrubTimestamp, startRow, endRow);
        final BatchingVisitable<RowResult<Value>> results = BatchingVisitableFromIterable.create(iterator);
        return BatchingVisitableView.of(
                new AbstractBatchingVisitable<SortedMap<Long, Multimap<TableReference, Cell>>>() {
                    @Override
                    protected <K extends Exception> void batchAcceptSizeHint(
                            int batchSizeHint,
                            ConsistentVisitor<SortedMap<Long, Multimap<TableReference, Cell>>, K> visitor) throws K {
                        results.batchAccept(cellsToScrubBatchSize, batch ->
                                visitor.visit(ImmutableList.of(transformRows(batch))));
                    }
                });
    }
