
	public BerkeleyDBFreenetStore(String storeDir, long maxChkBlocks, int blockSize, int headerSize, boolean throwOnTooFewKeys) throws IOException, DatabaseException {
		logMINOR = Logger.shouldLog(Logger.MINOR, this);
		this.dataBlockSize = blockSize;
		this.headerBlockSize = headerSize;
		this.freeBlocks = new SortedLongSet();
		// Percentage of the database that must contain usefull data
		// decrease to increase performance, increase to save disk space
		System.setProperty("je.cleaner.minUtilization","98");
		
		// Delete empty log files
		System.setProperty("je.cleaner.expunge","true");
		
		// Percentage of the maximum heap size used as a cache
		System.setProperty("je.maxMemoryPercent","30");
		
		this.maxChkBlocks=maxChkBlocks;
		
		File dir = new File(storeDir);
		if(!dir.exists())
			dir.mkdir();
		File dbDir = new File(dir,"database");
		if(!dbDir.exists())
			dbDir.mkdir();
		
		Environment env = null;
		// Initialize environment
		try {
			EnvironmentConfig envConfig = new EnvironmentConfig();
			envConfig.setAllowCreate(true);
			envConfig.setTransactional(true);
			envConfig.setTxnWriteNoSync(true);
			env = new Environment(dbDir, envConfig);
		} catch (DatabaseException e) {
			if(env != null)
				env.close();
			throw e;
		}
		environment = env;
		
		// Initialize CHK database
		DatabaseConfig dbConfig = new DatabaseConfig();
		dbConfig.setAllowCreate(true);
		dbConfig.setTransactional(true);
		chkDB = environment.openDatabase(null,"CHK",dbConfig);
		
		fixSecondaryFile = new File(storeDir, "recreate_secondary_db");
		
		if(fixSecondaryFile.exists()) {
			fixSecondaryFile.delete();
			Logger.error(this, "Recreating secondary database for "+storeDir);
			Logger.error(this, "This may take some time...");
			System.err.println("Recreating secondary database for "+storeDir);
			System.err.println("This may take some time...");
			try {
				environment.truncateDatabase(null, "CHK_accessTime", false);
			} catch (DatabaseException e) {
				close(false);
				throw e;
			}
		}
		
		// Initialize secondary CHK database sorted on accesstime
		SecondaryConfig secDbConfig = new SecondaryConfig();
		secDbConfig.setAllowCreate(true);
		secDbConfig.setSortedDuplicates(true);
		secDbConfig.setTransactional(true);
		secDbConfig.setAllowPopulate(true);
		storeBlockTupleBinding = new StoreBlockTupleBinding();
		longTupleBinding = TupleBinding.getPrimitiveBinding(Long.class);
		AccessTimeKeyCreator accessTimeKeyCreator = 
			new AccessTimeKeyCreator(storeBlockTupleBinding);
		secDbConfig.setKeyCreator(accessTimeKeyCreator);
		try {
			chkDB_accessTime = environment.openSecondaryDatabase
								(null, "CHK_accessTime", chkDB, secDbConfig);
		} catch (DatabaseException e1) {
			close(false);
			throw e1;
		}
		
		// Initialize other secondary database sorted on block number
//		try {
//			environment.removeDatabase(null, "CHK_blockNum");
//		} catch (DatabaseNotFoundException e) { };
		SecondaryConfig blockNoDbConfig = new SecondaryConfig();
		blockNoDbConfig.setAllowCreate(false);
		blockNoDbConfig.setSortedDuplicates(false);
		blockNoDbConfig.setAllowPopulate(true);
		blockNoDbConfig.setTransactional(true);
		
		BlockNumberKeyCreator bnkc = 
			new BlockNumberKeyCreator(storeBlockTupleBinding);
		blockNoDbConfig.setKeyCreator(bnkc);
		SecondaryDatabase blockNums;
		try {
			System.err.println("Opening block db index");
			blockNums = environment.openSecondaryDatabase
				(null, "CHK_blockNum", chkDB, blockNoDbConfig);
		} catch (DatabaseNotFoundException e) {
			System.err.println("Migrating block db index");
			// De-dupe on keys and block numbers.
			migrate(storeDir);
			System.err.println("De-duped, creating new index...");
			blockNoDbConfig.setSortedDuplicates(false);
			blockNoDbConfig.setAllowCreate(true);
			blockNoDbConfig.setAllowPopulate(true);
			blockNums = environment.openSecondaryDatabase
				(null, "CHK_blockNum", chkDB, blockNoDbConfig);
		} catch (DatabaseException e) {
			close(false);
			throw e;
		}
		
		chkDB_blockNum = blockNums;
		
		// Initialize the store file
		File storeFile = new File(dir,"store");
		try {
			if(!storeFile.exists())
				storeFile.createNewFile();
			chkStore = new RandomAccessFile(storeFile,"rw");
			
			long chkBlocksInDatabase = countCHKBlocksFromDatabase();
			chkBlocksInStore = chkBlocksInDatabase;
			long chkBlocksFromFile = countCHKBlocksFromFile();
			lastRecentlyUsed = getMaxRecentlyUsed();
			
			if(((chkBlocksInStore == 0) && (chkBlocksFromFile != 0)) ||
					(((chkBlocksInStore + 10) * 1.1) < chkBlocksFromFile)) {
				if(throwOnTooFewKeys) {
					try {
						close(false);
					} catch (Throwable t) {
						Logger.error(this, "Failed to close: "+t, t);
						System.err.println("Failed to close: "+t);
						t.printStackTrace();
					}
					throw new DatabaseException("Keys in database: "+chkBlocksInStore+" but keys in file: "+chkBlocksFromFile);
				} else {
					long len = checkForHoles(chkBlocksFromFile);
					if(len < chkBlocksFromFile) {
						System.err.println("Truncating to "+len+" as no non-holes after that point");
						chkStore.setLength(len * (dataBlockSize + headerBlockSize));
						chkBlocksInStore = len;
					}
				}
			}
			
			chkBlocksInStore = Math.max(chkBlocksInStore, chkBlocksFromFile);
			if(logMINOR) Logger.minor(this, "Keys in store: "+chkBlocksInStore);
			System.out.println("Keys in store: "+chkBlocksInStore+" / "+maxChkBlocks+" (db "+chkBlocksInDatabase+" file "+chkBlocksFromFile+")");

			maybeShrink(true, true);
			
//			 Add shutdownhook
			Runtime.getRuntime().addShutdownHook(new ShutdownHook());
		} catch (DatabaseException t) {
			Logger.error(this, "Caught "+t, t);
			close(false);
			throw t;
		} catch (IOException t) {
			Logger.error(this, "Caught "+t, t);
			close(false);
			throw t;
		}
	}