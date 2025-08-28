    public void loadCopyFile(DatabaseContext dbCtx, File copyFile, String tableName) {
    	CopyManager copyManager;
    	InputStream inStream = null;
    	
    	try {
    		InputStream bufferedInStream;
    		
    		inStream = new FileInputStream(copyFile);
    		bufferedInStream = new BufferedInputStream(inStream, 65536);
    		
    		copyManager = new CopyManager((BaseConnection) dbCtx.getConnection());
    		
    		copyManager.copyIn("COPY " + tableName + " FROM STDIN", bufferedInStream);
			
    		inStream.close();
			inStream = null;
			
    	} catch (IOException e) {
    		throw new OsmosisRuntimeException("Unable to process COPY file " + copyFile + ".", e);
    	} catch (SQLException e) {
    		throw new OsmosisRuntimeException("Unable to process COPY file " + copyFile + ".", e);
    	} finally {
    		if (inStream != null) {
				try {
					inStream.close();
				} catch (Exception e) {
					LOG.log(Level.SEVERE, "Unable to close COPY file.", e);
				}
				inStream = null;
			}
    	}
    }
