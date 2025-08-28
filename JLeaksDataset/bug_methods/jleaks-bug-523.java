	public Bucket readFilter(Bucket data, BucketFactory bf, String charset,
			HashMap otherParams, FilterCallback cb) throws DataFilterException,
			IOException {
		InputStream is = data.getInputStream();
		BufferedInputStream bis = new BufferedInputStream(is);
		DataInputStream dis = new DataInputStream(bis);
		// Check the header
		byte[] headerCheck = new byte[pngHeader.length];
		dis.read(headerCheck);
		if(!Arrays.equals(headerCheck, pngHeader)) {
			// Throw an exception
			String message = "The file you tried to fetch is not a PNG. It does not include a valid PNG header. "+
					"It might be some other file format, and your browser may do something horrible with it, "+
					"therefore we have blocked it."; 
			throw new DataFilterException("Not a PNG - invalid header", "Not a PNG - invalid header",
					"<p>"+message+"</p>", new HTMLNode("p").addChild("#", message));
		}
		dis.close();
		return data;
	}
