	private Template createTemplate(Resource resource) throws IOException {
		Reader reader = getReader(resource);
		try {
			return this.compiler.compile(reader);
		}
		finally {
			reader.close();
		}
	}
