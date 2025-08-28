	public String downloadRemoteTextFile() throws IOException {
		BufferedReader  bufferedReader = new BufferedReader(new InputStreamReader(stream));
		StringBuilder	htmlBuilder = new StringBuilder();
		String			line;

		while ((line = bufferedReader.readLine()) != null) {
			htmlBuilder.append(line);
			htmlBuilder.append('\n');
		}

		return htmlBuilder.toString();
	}
