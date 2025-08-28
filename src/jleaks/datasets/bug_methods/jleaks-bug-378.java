	public void load(File file) throws IOException {
		InputStream inStream = new FileInputStream(file);
		properties.load(inStream);
	}
