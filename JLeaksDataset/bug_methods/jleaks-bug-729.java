    void dump( File storeFile ) throws IOException
    {
        DefaultIdGeneratorFactory idGeneratorFactory = new DefaultIdGeneratorFactory();
        DefaultFileSystemAbstraction fs = new DefaultFileSystemAbstraction();
        LifeSupport life = new LifeSupport();
        life.start();
        PageCache pageCache = createPageCache( fs, "dump-store-chain-tool", life );
        StoreFactory storeFactory = new StoreFactory( new Config(), idGeneratorFactory, pageCache, fs, logger(), null );
        RecordStore<RECORD> store = store( storeFactory, storeFile );
        try
        {
            for ( long next = first; next != -1; )
            {
                RECORD record = store.forceGetRecord( next );
                System.out.println( record );
                next = next( record );
            }
        }
        finally
        {
            store.close();
            life.shutdown();
        }
    }
