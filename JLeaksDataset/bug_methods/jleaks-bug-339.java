	private static void addFilesToCompression(TarArchiveOutputStream taos, File file, String dir, String bundleRoot)
		throws IOException
	{
	    	if(!file.isHidden()) {
	    		// Create an entry for the file
	    		if(!dir.equals("."))
	    			if(File.separator.equals("\\")){
	    				dir = dir.replaceAll("\\\\", "/");
	    			}
	    			taos.putArchiveEntry(new TarArchiveEntry(file, dir + "/" + file.getName()));
				if (file.isFile()) {
			        // Add the file to the archive
					BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
					IOUtils.copy(new FileInputStream(file), taos);
					taos.closeArchiveEntry();
					bis.close();
				} else if (file.isDirectory()) {
					//Logger.info(this.getClass(),file.getPath().substring(bundleRoot.length()));
			         // close the archive entry
					if(!dir.equals("."))
						taos.closeArchiveEntry();
			         // go through all the files in the directory and using recursion, add them to the archive
					for (File childFile : file.listFiles()) {
						addFilesToCompression(taos, childFile, file.getPath().substring(bundleRoot.length()), bundleRoot);
					}
				}
	    	}
	    
	}
