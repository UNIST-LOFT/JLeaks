	BinaryBlobInserter(Bucket blob, ClientPutter parent, RequestClient clientContext, boolean tolerant, short prioClass, InsertContext ctx, ClientContext context, ObjectContainer container)
	throws IOException, BinaryBlobFormatException {
		logMINOR = Logger.shouldLog(LogLevel.MINOR, this);
		this.ctx = ctx;
		this.maxRetries = ctx.maxInsertRetries;
		this.consecutiveRNFsCountAsSuccess = ctx.consecutiveRNFsCountAsSuccess;
		this.parent = parent;
		this.clientContext = clientContext;
		this.errors = new FailureCodeTracker(true);
		DataInputStream dis = new DataInputStream(blob.getInputStream());

		BlockSet blocks = new SimpleBlockSet();

		BinaryBlob.readBinaryBlob(dis, blocks, tolerant);

		dis.close();

		ArrayList<MySendableInsert> myInserters = new ArrayList<MySendableInsert>();
		Iterator i = blocks.keys().iterator();

		int x=0;
		while(i.hasNext()) {
			Key key = (Key) i.next();
			KeyBlock block = blocks.get(key);
			MySendableInsert inserter =
				new MySendableInsert(x++, block, prioClass, getScheduler(block, context), clientContext);
			myInserters.add(inserter);
		}

		inserters = myInserters.toArray(new MySendableInsert[myInserters.size()]);
		parent.addMustSucceedBlocks(inserters.length, container);
		parent.notifyClients(container, context);
	}
