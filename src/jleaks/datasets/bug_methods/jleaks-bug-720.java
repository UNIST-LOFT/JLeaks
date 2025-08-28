    public InputStream getEntryInputStream(final ArchiveEntry entry, ArchiveEntryIterator entryIterator) throws IOException, UnsupportedFileOperationException {
		checkSevenZipFile();
		
		final FailSafePipedInputStream in = new FailSafePipedInputStream();
		final PipedOutputStream out = new PipedOutputStream(in);

		threadPool.execute(new Runnable() {
			public void run() {
		        BufferedOutputStream bufferStream = new BufferedOutputStream(out);
		        try {
					MuArchiveExtractCallback extractCallbackSpec = new MuArchiveExtractCallback(bufferStream, entry.getPath());
			        extractCallbackSpec.Init(sevenZipFile);
			        sevenZipFile.Extract(null, -1, IInArchive.NExtract_NAskMode_kExtract , extractCallbackSpec);
			        
					bufferStream.flush();
				} catch (Exception e) {
					// TODO Auto-generated catch block
                    FileLogger.finest(null, e);
				}
		        
			}
		});
		
		return in; 
	}
