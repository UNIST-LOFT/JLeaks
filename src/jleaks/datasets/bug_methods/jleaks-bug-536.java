	public void save(Factory f, OutputStream out) throws IOException {
		if (f.getEnvironment().getCompressionType() == CompressionType.GZIP) {
			out = new GZIPOutputStream(out);
		}
		ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(out));
		oos.writeObject(f);
		oos.flush();
		oos.close();
	}
