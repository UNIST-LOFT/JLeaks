    public void copyStore( StoreCopyRequester requester ) throws IOException
    {
        // Clear up the current temp directory if there
        File storeDir = config.get( InternalAbstractGraphDatabase.Configuration.store_dir );
        File tempStore = new File( storeDir, COPY_FROM_MASTER_TEMP );
        Config tempConfig = configForTempStore( tempStore );

        if ( !tempStore.mkdir() )
        {
            FileUtils.deleteRecursively( tempStore );
            tempStore.mkdir();
        }

        // Request store files and transactions that will need recovery
        Response response = requester.copyStore( decorateWithProgressIndicator(new ToFileStoreWriter( tempStore )));

        // Update highest archived log id
        long highestLogVersion = XaLogicalLog.getHighestHistoryLogVersion( fs, tempStore, LOGICAL_LOG_DEFAULT_NAME );
        if ( highestLogVersion > -1 )
        {
            NeoStore.setVersion( fs, tempStore, highestLogVersion + 1 );
        }

        // Write pending transactions down to the currently active logical log
        writeTransactionsToActiveLogFile( tempConfig, response.transactions() );
        response.close();
        requester.done();

        // Run recovery
        GraphDatabaseAPI copiedDb = newTempDatabase( tempStore );
        copiedDb.shutdown();

        // All is well, move to the real store directory
        for ( File candidate : tempStore.listFiles( new FileFilter()
        {
            @Override
            public boolean accept( File file )
            {
                // Skip log files and tx files from temporary database
                return !file.getName().startsWith( "metrics" )
                        && !file.getName().equals( StringLogger.DEFAULT_NAME )
                        && !("active_tx_log tm_tx_log.1 tm_tx_log.2").contains( file.getName() );
            }
        } ) )
        {
            FileUtils.moveFileToDirectory( candidate, storeDir );
        }
    }
