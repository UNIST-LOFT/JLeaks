	private String createTarBucket(Bucket outputBucket, @SuppressWarnings("unused") ObjectContainer container) throws IOException {
		if(logMINOR) Logger.minor(this, "Create a TAR Bucket");
		
		OutputStream os = new BufferedOutputStream(outputBucket.getOutputStream());
		TarOutputStream tarOS = new TarOutputStream(os);
		tarOS.setLongFileMode(TarOutputStream.LONGFILE_GNU);
		TarEntry ze;

		for(ContainerElement ph : containerItems) {
			if(logMINOR)
				Logger.minor(this, "Putting into tar: "+ph+" data length "+ph.data.size()+" name "+ph.targetInArchive);
			ze = new TarEntry(ph.targetInArchive);
			ze.setModTime(0);
			long size = ph.data.size();
			ze.setSize(size);
			tarOS.putNextEntry(ze);
			BucketTools.copyTo(ph.data, tarOS, size);
			tarOS.closeEntry();
		}

		tarOS.closeEntry();
		// Both finish() and close() are necessary.
		tarOS.finish();
		tarOS.flush();
		tarOS.close();
		
		if(logMINOR)
			Logger.minor(this, "Archive size is "+outputBucket.size());
		
		return ARCHIVE_TYPE.TAR.mimeTypes[0];
	}
	
	private String createZipBucket(Bucket outputBucket, @SuppressWarnings("unused") ObjectContainer container) throws IOException {
		if(logMINOR) Logger.minor(this, "Create a ZIP Bucket");
		
		OutputStream os = new BufferedOutputStream(outputBucket.getOutputStream());
		ZipOutputStream zos = new ZipOutputStream(os);
		ZipEntry ze;

		for(ContainerElement ph: containerItems) {
			ze = new ZipEntry(ph.targetInArchive);
			ze.setTime(0);
			zos.putNextEntry(ze);
			BucketTools.copyTo(ph.data, zos, ph.data.size());
			zos.closeEntry();
		}

		zos.closeEntry();
		// Both finish() and close() are necessary.
		zos.finish();
		zos.flush();
		zos.close();
		
		return ARCHIVE_TYPE.ZIP.mimeTypes[0];
	}
