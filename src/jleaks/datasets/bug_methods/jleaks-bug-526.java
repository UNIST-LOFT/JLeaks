	public Bucket compress(Bucket data, BucketFactory bf, long maxLength) throws IOException, CompressionOutputSizeException {
		Bucket output = bf.makeBucket(-1);
		InputStream is = data.getInputStream();
		OutputStream os = output.getOutputStream();
		GZIPOutputStream gos = new GZIPOutputStream(os);
		long written = 0;
		byte[] buffer = new byte[4096];
		while(true) {
			int l = (int) Math.min(buffer.length, maxLength - written);
			int x = is.read(buffer, 0, buffer.length);
			if(l < x) {
				throw new CompressionOutputSizeException();
			}
			if(x <= -1) break;
			if(x == 0) throw new IOException("Returned zero from read()");
			gos.write(buffer, 0, x);
			written += x;
		}
		is.close();
		gos.close();
		return output;
	}
