    private void closeStore(boolean shrinkIfPossible) {
        if (closed) {
            return;
        }
        stopBackgroundThread();
        closed = true;
        storeLock.lock();
        try {
            if (fileStore != null && shrinkIfPossible) {
                shrinkFileIfPossible(0);
            }
            // release memory early - this is important when called
            // because of out of memory
            if (cache != null) {
                cache.clear();
            }
            if (cacheChunkRef != null) {
                cacheChunkRef.clear();
            }
            for (MVMap<?, ?> m : new ArrayList<>(maps.values())) {
                m.close();
            }
            chunks.clear();
            maps.clear();
            if (fileStore != null && !fileStoreIsProvided) {
                fileStore.close();
            }
        } finally {
            storeLock.unlock();
        }
    }
