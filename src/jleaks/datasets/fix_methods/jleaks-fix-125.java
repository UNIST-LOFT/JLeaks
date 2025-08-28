protected long stampSequenceIdAndPublishToRingBuffer(RegionInfo hri, WALKey key, WALEdit edits,
boolean inMemstore, RingBuffer<RingBufferTruck> ringBuffer)
throws IOException {
    if (this.closed) {
        throw new IOException("Cannot append; log is closed, regionName = " + hri.getRegionNameAsString());
    }
    MutableLong txidHolder = new MutableLong();
    MultiVersionConcurrencyControl.WriteEntry we = key.getMvcc().begin(() -> {
        txidHolder.setValue(ringBuffer.next());
    });
    long txid = txidHolder.longValue();
    try (TraceScope scope = TraceUtil.createTrace(implClassName + ".append")) {
        FSWALEntry entry = new FSWALEntry(txid, key, edits, hri, inMemstore);
        entry.stampRegionSequenceId(we);
        if (scope != null) {
            ringBuffer.get(txid).load(entry, scope.getSpan());
        } else {
            ringBuffer.get(txid).load(entry, null);
        }
    } finally {
        ringBuffer.publish(txid);
    }
    return txid;
}