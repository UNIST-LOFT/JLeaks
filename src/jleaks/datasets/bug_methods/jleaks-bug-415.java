	public void setConfiguration( String policyFilePath, String webRootDir ) throws FileNotFoundException {
		try {
			appGuardConfig = ConfigurationParser.readConfigurationFile(new FileInputStream(new File(policyFilePath)), webRootDir);
			lastConfigReadTime = System.currentTimeMillis();
			configurationFilename = policyFilePath;
		} catch (ConfigurationException e ) {
            // TODO: It would be ideal if this method through the ConfigurationException rather than catching it and
            // writing the error to the console.
			e.printStackTrace();
		}
	}
