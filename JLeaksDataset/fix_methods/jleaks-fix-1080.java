
	public void load(File file) throws IOException {
		try (BufferedReader in = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = in.readLine()) != null) {
				this.addToken(line.trim());
			}
		}
	}