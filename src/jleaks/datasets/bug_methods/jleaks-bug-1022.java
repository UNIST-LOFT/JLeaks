  public static Properties loadProperties(File propertiesFile) {
    FileInputStream propertiesStream = null;
    Properties properties = new Properties();
    try {
      propertiesStream = new FileInputStream(propertiesFile);
    } catch (FileNotFoundException e) {
      try {
        LOG.info(String.format("No configuration file found (%s)", propertiesFile.getCanonicalPath()));
      } catch (IOException ioe) {
        LOG.info(String.format("No configuration file found (%s)", propertiesFile));
      }
    }

    if (propertiesStream != null) {
      try {
        properties.load(propertiesStream);
        propertiesStream.close();
      } catch (IOException e) {
        LOG.log(Level.WARNING, String.format("Error reading configuration: %s", e.getMessage()));
      }
    }

    return properties;
  }
