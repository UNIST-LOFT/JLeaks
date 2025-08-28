	public static int toByteArray(Bucket bucket, byte[] output) throws IOException {
		long size = bucket.size();
		if(size > output.length)
			throw new IllegalArgumentException("Data does not fit in provided buffer");
		InputStream is = bucket.getInputStream();
		int moved = 0;
		while(true) {
			if(moved == size) return moved;
			int x = is.read(output, moved, (int)(size - moved));
			if(x == -1) return moved;
			moved += x;
		}
	}
