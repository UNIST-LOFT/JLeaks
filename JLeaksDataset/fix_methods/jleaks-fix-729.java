void dump( File storeFile ) throws IOException{
    DefaultFileSystemAbstraction fs = new DefaultFileSystemAbstraction();
    try (StandalonePageCache pageCache = createPageCache(fs, "dump-store-chain-tool")) {
        DefaultIdGeneratorFactory idGeneratorFactory = new DefaultIdGeneratorFactory();
        Config config = new Config();
        StoreFactory storeFactory = new StoreFactory(config, idGeneratorFactory, pageCache, fs, logger(), null);
        RecordStore<RECORD> store = store(storeFactory, storeFile);
        try {
            for (long next = first; next != -1; ) {
                RECORD record = store.forceGetRecord(next);
                System.out.println(record);
                next = next(record);
            }
        } finally {
            store.close();
        }
    }
}