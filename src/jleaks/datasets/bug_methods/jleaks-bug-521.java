	private void parseMultiPartData() throws IOException {
		if(data == null) return;
		String ctype = (String) this.headers.get("content-type");
		if (ctype == null) return;
		if(Logger.shouldLog(Logger.MINOR, this))
			Logger.minor(this, "Uploaded content-type: "+ctype);
		String[] ctypeparts = ctype.split(";");
		if(ctypeparts[0].equalsIgnoreCase("application/x-www-form-urlencoded")) {
			// Completely different encoding, but easy to handle
			if(data.size() > 1024*1024)
				throw new IOException("Too big");
			byte[] buf = BucketTools.toByteArray(data);
			String s = new String(buf, "us-ascii");
			parseRequestParameters(s, true, true);
		}
		if (!ctypeparts[0].trim().equalsIgnoreCase("multipart/form-data") || (ctypeparts.length < 2)) {
			return;
		}
		
		String boundary = null;
		for (int i = 0; i < ctypeparts.length; i++) {
			String[] subparts = ctypeparts[i].split("=");
			if ((subparts.length == 2) && subparts[0].trim().equalsIgnoreCase("boundary")) {
				boundary = subparts[1];
			}
		}
		
		if ((boundary == null) || (boundary.length() == 0)) return;
		if (boundary.charAt(0) == '"') boundary = boundary.substring(1);
		if (boundary.charAt(boundary.length() - 1) == '"')
			boundary = boundary.substring(0, boundary.length() - 1);
		
		boundary = "--"+boundary;
		
		InputStream is = this.data.getInputStream();
		BufferedInputStream bis = new BufferedInputStream(is, 32768);
		LineReadingInputStream lis = new LineReadingInputStream(bis);
		
		String line;
		line = lis.readLine(100, 100, false); // really it's US-ASCII, but ISO-8859-1 is close enough.
		while ((bis.available() > 0) && !line.equals(boundary)) {
			line = lis.readLine(100, 100, false);
		}
		
		boundary  = "\r\n"+boundary;
		
		Bucket filedata = null;
		String name = null;
		String filename = null;
		String contentType = null;
		
		while(bis.available() > 0) {
			name = null;
			filename = null;
			contentType = null;
			// chomp headers
			while( (line = lis.readLine(200, 200, true)) /* should be UTF-8 as we told the browser to send UTF-8 */ != null) {
				if (line.length() == 0) break;
				
				String[] lineparts = line.split(":");
				if (lineparts == null || lineparts.length == 0) continue;
				String hdrname = lineparts[0].trim();
				
				if (hdrname.equalsIgnoreCase("Content-Disposition")) {
					if (lineparts.length < 2) continue;
					String[] valueparts = lineparts[1].split(";");
					
					for (int i = 0; i < valueparts.length; i++) {
						String[] subparts = valueparts[i].split("=");
						if (subparts.length != 2) {
							continue;
						}
						String fieldname = subparts[0].trim();
						String value = subparts[1].trim();
						if (value.startsWith("\"") && value.endsWith("\"")) {
							value = value.substring(1, value.length() - 1);
						}
						if (fieldname.equalsIgnoreCase("name")) {
							name = value;
						} else if (fieldname.equalsIgnoreCase("filename")) {
							filename = value;
						}
					}
				} else if (hdrname.equalsIgnoreCase("Content-Type")) {
					contentType = lineparts[1].trim();
					if(Logger.shouldLog(Logger.MINOR, this)) Logger.minor(this, "Parsed type: "+contentType);
				} else {
					// Do nothing, irrelevant header
				}
			}
			
			if (name == null) continue;
			
			// we should be at the data now. Start reading it in, checking for the
			// boundary string
			
			// we can only give an upper bound for the size of the bucket
			filedata = this.bucketfactory.makeBucket(bis.available());
			OutputStream bucketos = filedata.getOutputStream();
			OutputStream bbos = new BufferedOutputStream(bucketos, 32768);
			// buffer characters that match the boundary so far
			// FIXME use whatever charset was used
			byte[] bbound = boundary.getBytes("UTF-8"); // ISO-8859-1? boundary should be in US-ASCII
			int offset = 0;
			while ((bis.available() > 0) && (offset < bbound.length)) {
				byte b = (byte)bis.read();
				
				if (b == bbound[offset]) {
					offset++;
				} else if ((b != bbound[offset]) && (offset > 0)) {
					// offset bytes matched, but no more
					// write the bytes that matched, then the non-matching byte
					bbos.write(bbound, 0, offset);
					bbos.write((int) b & 0xff);
					offset = 0;
				} else {
					bbos.write((int) b & 0xff);
				}
			}
			
			bbos.close();
			
			parts.put(name, filedata);
			if(Logger.shouldLog(Logger.MINOR, this))
				Logger.minor(this, "Name = "+name+" length = "+filedata.size()+" filename = "+filename);
			if (filename != null) {
				uploadedFiles.put(name, new HTTPUploadedFileImpl(filename, contentType, filedata));
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see freenet.clients.http.HTTPRequest#getUploadedFile(java.lang.String)
	 */
	public HTTPUploadedFile getUploadedFile(String name) {
		return (HTTPUploadedFile) uploadedFiles.get(name);
	}
	
	/* (non-Javadoc)
	 * @see freenet.clients.http.HTTPRequest#getPart(java.lang.String)
	 */
	public Bucket getPart(String name) {
		return (Bucket)this.parts.get(name);
	}
	
	/* (non-Javadoc)
	 * @see freenet.clients.http.HTTPRequest#isPartSet(java.lang.String)
	 */
	public boolean isPartSet(String name) {
		return this.parts.containsKey(name);
	}
	
	/* (non-Javadoc)
	 * @see freenet.clients.http.HTTPRequest#getPartAsString(java.lang.String, int)
	 */
	public String getPartAsString(String name, int maxlength) {
		Bucket part = (Bucket)this.parts.get(name);
		if(part == null) return "";
		
		if (part.size() > maxlength) return "";
		
		try {
			InputStream is = part.getInputStream();
			DataInputStream dis = new DataInputStream(is);
			byte[] buf = new byte[is.available()];
			dis.readFully(buf);
			is.close();
			return new String(buf);
		} catch (IOException ioe) {
			
		}
		return "";
	}
