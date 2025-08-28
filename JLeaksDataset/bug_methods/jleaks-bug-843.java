	static public void copyToFile(byte[] src, SFile dest) throws IOException {
		final OutputStream fos = dest.createBufferedOutputStream();
		fos.write(src);
		fos.close();
	}
