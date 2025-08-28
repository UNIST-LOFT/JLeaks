public CloseableIterator<Entry<KeyType>> combine(
    List<? extends CloseableIterator<Entry<KeyType>>> sortedIterators,
    List<String> mergedDictionary
)
{
    // CombineBuffer is initialized when this method is called and closed after the result iterator is done
    final Closer closer = Closer.create();
    final ResourceHolder<ByteBuffer> combineBufferHolder = combineBufferSupplier.get();
    closer.register(combineBufferHolder);
    try {
        final ByteBuffer combineBuffer = combineBufferHolder.get();
        final int minimumRequiredBufferCapacity = StreamingMergeSortedGrouper.requiredBufferCapacity(combineKeySerdeFactory.factorizeWithDictionary(mergedDictionary), combiningFactories);
        // We want to maximize the parallelism while the size of buffer slice is greater than the minimum buffer size
        // required by StreamingMergeSortedGrouper. Here, we find the leafCombineDegree of the cominbing tree and the
        // required number of buffers maximizing the parallelism.
        final Pair<Integer, Integer> degreeAndNumBuffers = findLeafCombineDegreeAndNumBuffers(combineBuffer, minimumRequiredBufferCapacity, concurrencyHint, sortedIterators.size());
        final int leafCombineDegree = degreeAndNumBuffers.lhs;
        final int numBuffers = degreeAndNumBuffers.rhs;
        final int sliceSize = combineBuffer.capacity() / numBuffers;
        final Supplier<ByteBuffer> bufferSupplier = createCombineBufferSupplier(combineBuffer, numBuffers, sliceSize);
        final Pair<List<CloseableIterator<Entry<KeyType>>>, List<Future>> combineIteratorAndFutures = buildCombineTree(sortedIterators, bufferSupplier, combiningFactories, leafCombineDegree, mergedDictionary);
        final CloseableIterator<Entry<KeyType>> combineIterator = Iterables.getOnlyElement(combineIteratorAndFutures.lhs);
        final List<Future> combineFutures = combineIteratorAndFutures.rhs;
        closer.register(() -> checkCombineFutures(combineFutures));
        return CloseableIterators.wrap(combineIterator, closer);
    } catch (Throwable t) {
        try {
            closer.close();
        } catch (Throwable t2) {
            t.addSuppressed(t2);
        }
        throw t;
    }
}