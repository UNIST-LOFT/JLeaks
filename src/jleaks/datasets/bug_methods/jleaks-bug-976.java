	private static List<RealClass> readRealClassesFromJarFile(File jarFile)
			throws JarFileParsingException, FileNotFoundException {
		if (!jarFile.exists()) {
			throw new FileNotFoundException("Attempted to load jar file at: "
					+ jarFile + " but it does not exist.");
		}
		try {
			ZipFile zipFile = new ZipFile(jarFile);
			List<RealClass> realClasses = readJarFile(zipFile);
			zipFile.close();
			return realClasses;
		} catch (IOException e) {
			throw new JarFileParsingException("Error extracting jar data.", e);
		} catch (RealClassCreationException e) {
			throw new JarFileParsingException("Error extracting jar data.", e);
		}
	}
