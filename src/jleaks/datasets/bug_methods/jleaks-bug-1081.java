	public void save(File file) throws IOException {
		BufferedWriter out = new BufferedWriter(new FileWriter(file));
		for (String token : tokens) {
			out.write(token + "\n");
		}
		out.close();
	}
