	private void saveToFile(ResContainer rc, File outFile) {
		CodeWriter cw = rc.getContent();
		if (cw != null) {
			cw.save(outFile);
			return;
		}
		InputStream binary = rc.getBinary();
		if(binary != null) {
			try {
				outFile.getParentFile().mkdirs();
				FileOutputStream binaryFileStream = new FileOutputStream(outFile);
				IOUtils.copy(binary, binaryFileStream);
				binaryFileStream.close();
				binary.close();
			}
			catch(IOException e) {
				LOG.warn("Resource '{}' not saved, got exception {}", rc.getName(), e);
			}
			return;
		}
		LOG.warn("Resource '{}' not saved, unknown type", rc.getName());
	}
