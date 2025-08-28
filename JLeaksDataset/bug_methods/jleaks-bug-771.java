	private File downloadReplicationFile(String fileName, URL baseUrl) {
		URL changesetUrl;
		InputStream inputStream = null;
		OutputStream outputStream = null;
		
		try {
			changesetUrl = new URL(baseUrl, fileName);
		} catch (MalformedURLException e) {
			throw new OsmosisRuntimeException("The server file URL could not be created.", e);
		}
		
		try {
			BufferedInputStream source;
			BufferedOutputStream sink;
			File outputFile;
			byte[] buffer;
			
			// Open an input stream for the changeset file on the server.
			URLConnection connection = changesetUrl.openConnection();
			connection.setReadTimeout(15 * 60 * 1000); // timeout 15 minutes
			connection.setConnectTimeout(15 * 60 * 1000); // timeout 15 minutes
			inputStream = connection.getInputStream();
			source = new BufferedInputStream(inputStream, 65536);
			
			// Create a temporary file to write the data to.
			outputFile = File.createTempFile("change", null);
			
			// Open a output stream for the destination file.
			outputStream = new FileOutputStream(outputFile);
			sink = new BufferedOutputStream(outputStream, 65536);
			
			// Download the file.
			buffer = new byte[65536];
			for (int bytesRead = source.read(buffer); bytesRead > 0; bytesRead = source.read(buffer)) {
				sink.write(buffer, 0, bytesRead);
			}
			sink.flush();
			
			// Clean up all file handles.
			inputStream.close();
			inputStream = null;
			outputStream.close();
			outputStream = null;
			
			return outputFile;
			
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to read the changeset file " + fileName + " from the server.", e);
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (IOException e) {
				// We are already in an error condition so log and continue.
				LOG.log(Level.WARNING, "Unable to changeset download stream.", e);
			}
			try {
				if (outputStream != null) {
					outputStream.close();
				}
			} catch (IOException e) {
				// We are already in an error condition so log and continue.
				LOG.log(Level.WARNING, "Unable to changeset output stream.", e);
			}
		}
	}
