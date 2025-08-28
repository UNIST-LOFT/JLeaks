	    private static void extract(final ZipFile zipFile, final ZipEntry zipEntry, final File toDir) throws IOException {
	        final File file = new File(toDir, zipEntry.getName());

            checkSecurity(toDir,file);

	        final File parentDir = file.getParentFile();
	        if (! parentDir.exists()){
	            parentDir.mkdirs();
	        }
	        
	        BufferedInputStream bis = null;
	        BufferedOutputStream bos = null;
	        try{
	            final InputStream istr = zipFile.getInputStream(zipEntry);
	            bis = new BufferedInputStream(istr);
	            final OutputStream os = Files.newOutputStream(file.toPath());
	            bos  = new BufferedOutputStream(os);
	            IOUtils.copy(bis, bos);
	        } finally {
	            if (bis !=  null){
	                bis.close();
	            }
	            if (bos != null){
	                bos.close();
	            }
	        }
	    }
