	public void writeEntry(String entryName, InputStream inputStream) throws IOException {
		writeEntry(entryName, new InputStreamEntryWriter(inputStream, true));
	}
