	private Date getServerTimestamp(URL baseUrl) {
		URL timestampUrl;
		InputStream timestampStream = null;
		
		try {
			timestampUrl = new URL(baseUrl, SERVER_TSTAMP_FILE);
		} catch (MalformedURLException e) {
			throw new OsmosisRuntimeException("The server timestamp URL could not be created.", e);
		}
		
		try {
			BufferedReader reader;
			Date result;
			
			URLConnection connection = timestampUrl.openConnection();
			connection.setReadTimeout(15 * 60 * 1000); // timeout 15 minutes
			connection.setConnectTimeout(15 * 60 * 1000); // timeout 15 minutes
			timestampStream = connection.getInputStream();
			
			reader = new BufferedReader(new InputStreamReader(timestampStream));
			
			result = dateParser.parse(reader.readLine());
			
			timestampStream.close();
			timestampStream = null;
			
			return result;
			
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to read the timestamp from the server.", e);
		} finally {
			try {
				if (timestampStream != null) {
					timestampStream.close();
				}
			} catch (IOException e) {
				// We are already in an error condition so log and continue.
				LOG.log(Level.WARNING, "Unable to close timestamp stream.", e);
			}
		}
	}
