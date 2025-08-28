	public static String writeClasspathToFile(String classpath) {

		try {
			File file = File.createTempFile("EvoSuite_classpathFile",".txt");
			file.deleteOnExit();

			BufferedWriter out = new BufferedWriter(new FileWriter(file));
			String line = classpath;
			out.write(line);
			out.newLine();
			out.close();

			return file.getAbsolutePath();

		} catch (Exception e) {
			throw new IllegalStateException("Failed to create tmp file for classpath specification: "+e.getMessage());
		}
	}
