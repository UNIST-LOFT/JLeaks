public BatchingVisitable<SortedMap<Long, Multimap<TableReference, Cell>>> getBatchingVisitableScrubQueue(
    long maxScrubTimestamp /* exclusive */,
    byte[] startRow,
    byte[] endRow) {
    return new AbstractBatchingVisitable<SortedMap<Long, Multimap<TableReference, Cell>>>() {

        @Override
        protected <K extends Exception> void batchAcceptSizeHint(int batchSizeHint, ConsistentVisitor<SortedMap<Long, Multimap<TableReference, Cell>>, K> visitor) throws K {
            ClosableIterator<RowResult<Value>> iterator = getIteratorToScrub(batchSizeHint, maxScrubTimestamp, startRow, endRow);
            try {
                BatchingVisitableFromIterable.create(iterator).batchAccept(batchSizeHint, batch -> visitor.visitOne(transformRows(batch)));
            } finally {
                iterator.close();
            }
        }
    };
}