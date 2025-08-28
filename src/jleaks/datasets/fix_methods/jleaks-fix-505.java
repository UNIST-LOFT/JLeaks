public ClientGet(FCPClient globalClient, FreenetURI uri, boolean dsOnly, boolean ignoreDS,
			boolean filterData, int maxSplitfileRetries, int maxNonSplitfileRetries,
			long maxOutputLength, short returnType, boolean persistRebootOnly, String identifier, int verbosity,
			short prioClass, File returnFilename, String charset, boolean writeToClientCache, boolean realTimeFlag, FCPServer server) throws IdentifierCollisionException, NotAllowedException, IOException {
		super(uri, identifier, verbosity, charset, null, globalClient,
				prioClass,
				(persistRebootOnly ? ClientRequest.PERSIST_REBOOT : ClientRequest.PERSIST_FOREVER), realTimeFlag, null, true);

		fctx = new FetchContext(server.defaultFetchContext, FetchContext.IDENTICAL_MASK);
		fctx.eventProducer.addEventListener(this);
		fctx.localRequestOnly = dsOnly;
		fctx.ignoreStore = ignoreDS;
		fctx.maxNonSplitfileRetries = maxNonSplitfileRetries;
		fctx.maxSplitfileBlockRetries = maxSplitfileRetries;
		fctx.filterData = filterData;
		fctx.maxOutputLength = maxOutputLength;
		fctx.maxTempLength = maxOutputLength;
		fctx.canWriteClientCache = writeToClientCache;
		compatMode = new CompatibilityAnalyser();
		// FIXME fctx.ignoreUSKDatehints = ignoreUSKDatehints;
		Bucket ret = null;
		this.returnType = returnType;
		binaryBlob = false;
		String extensionCheck = null;
		if(returnType == ClientGetMessage.RETURN_TYPE_DISK) {
			this.targetFile = returnFilename;
			if(!(server.core.allowDownloadTo(returnFilename)))
				throw new NotAllowedException();
			ret = new FileBucket(returnFilename, false, true, false, false, false);
			if(filterData) {
				String name = returnFilename.getName();
				int idx = name.lastIndexOf('.');
				if(idx != -1) {
					idx++;
					if(idx != name.length())
						extensionCheck = name.substring(idx);
				}
			}
		} else if(returnType == ClientGetMessage.RETURN_TYPE_NONE) {
			targetFile = null;
			ret = new NullBucket();
		} else {
		    targetFile = null;
		    ret = null; // Let the ClientGetter allocate the Bucket later on.
		}
		getter = makeGetter(ret, null, null);
	}