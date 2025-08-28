	private Template createTemplate(Resource resource) throws IOException {
		return this.compiler.compile(getReader(resource));
	}
