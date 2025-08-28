public <OutType> Yielder<OutType> toYielder(OutType initValue, final YieldingAccumulator<OutType, T> accumulator){
    final CombiningYieldingAccumulator<OutType, T> combiningAccumulator = new CombiningYieldingAccumulator<>(ordering, mergeFn, accumulator);
    combiningAccumulator.setRetVal(initValue);
    final Yielder<T> baseYielder = baseSequence.toYielder(null, combiningAccumulator);
    try {
        return makeYielder(baseYielder, combiningAccumulator, false);
    } catch (Throwable t1) {
        try {
            baseYielder.close();
        } catch (Throwable t2) {
            t1.addSuppressed(t2);
        }
        throw t1;
    }
}