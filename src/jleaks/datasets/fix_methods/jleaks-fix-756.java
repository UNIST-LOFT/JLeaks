	protected void writeDOTFile(Grammar g, String name, String dot) throws IOException {
		Writer fw = getOutputFileWriter(g, name + ".dot");
		try {
			fw.write(dot);
		}
		finally {
			fw.close();
		}
	}
