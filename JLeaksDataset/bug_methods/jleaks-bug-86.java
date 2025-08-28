  public <OutType> Yielder<OutType> toYielder(OutType initValue, final YieldingAccumulator<OutType, T> accumulator)
  {
    final CombiningYieldingAccumulator<OutType, T> combiningAccumulator =
        new CombiningYieldingAccumulator<>(ordering, mergeFn, accumulator);

    combiningAccumulator.setRetVal(initValue);
    Yielder<T> baseYielder = baseSequence.toYielder(null, combiningAccumulator);

    return makeYielder(baseYielder, combiningAccumulator, false);
  }
