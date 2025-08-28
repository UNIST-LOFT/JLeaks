  protected long stampSequenceIdAndPublishToRingBuffer(RegionInfo hri, WALKey key, WALEdit edits,
      boolean inMemstore, RingBuffer<RingBufferTruck> ringBuffer)
      throws IOException {
    if (this.closed) {
      throw new IOException("Cannot append; log is closed, regionName = " + hri.getRegionNameAsString());
    }
    TraceScope scope = Trace.startSpan(implClassName + ".append");
    MutableLong txidHolder = new MutableLong();
    MultiVersionConcurrencyControl.WriteEntry we = key.getMvcc().begin(() -> {
      txidHolder.setValue(ringBuffer.next());
    });
    long txid = txidHolder.longValue();
    try {
      FSWALEntry entry = new FSWALEntry(txid, key, edits, hri, inMemstore);
      entry.stampRegionSequenceId(we);
      ringBuffer.get(txid).load(entry, scope.detach());
    } finally {
      ringBuffer.publish(txid);
    }
    return txid;
  }
