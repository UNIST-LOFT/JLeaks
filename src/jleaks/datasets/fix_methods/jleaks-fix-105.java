private boolean generalizedSeek(boolean isLazy, Cell seekKey,
boolean forward, boolean useBloom) throws IOException {
    if (!isLazy && useBloom) {
        throw new IllegalArgumentException("Multi-column Bloom filter " + "optimization requires a lazy seek");
    }
    if (current == null) {
        return false;
    }
    KeyValueScanner scanner = current;
    try {
        while (scanner != null) {
            Cell topKey = scanner.peek();
            if (comparator.getComparator().compare(seekKey, topKey) <= 0) {
                // Top KeyValue is at-or-after Seek KeyValue. We only know that all
                // scanners are at or after seekKey (because fake keys of
                // scanners where a lazy-seek operation has been done are not greater
                // than their real next keys) but we still need to enforce our
                // invariant that the top scanner has done a real seek. This way
                // StoreScanner and RegionScanner do not have to worry about fake
                // keys.
                heap.add(scanner);
                scanner = null;
                current = pollRealKV();
                return current != null;
            }
            boolean seekResult;
            if (isLazy && heap.size() > 0) {
                // If there is only one scanner left, we don't do lazy seek.
                seekResult = scanner.requestSeek(seekKey, forward, useBloom);
            } else {
                seekResult = NonLazyKeyValueScanner.doRealSeek(scanner, seekKey, forward);
            }
            if (!seekResult) {
                this.scannersForDelayedClose.add(scanner);
            } else {
                heap.add(scanner);
            }
            scanner = heap.poll();
            if (scanner == null) {
                current = null;
            }
        }
    } catch (Exception e) {
        if (scanner != null) {
            try {
                scanner.close();
            } catch (Exception ce) {
                LOG.warn("close KeyValueScanner error", ce);
            }
        }
        throw e;
    }
    // Heap is returning empty, scanner is done
    return false;
}