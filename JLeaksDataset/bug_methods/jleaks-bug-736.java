private void loadConfigsUsingClassLoader(String configFileName) throws Exception {
    BufferedInputStream fileInputStream = (BufferedInputStream) this
      .getClass().getClassLoader()
      .getResourceAsStream(configFileName);
    if (fileInputStream != null) {
      BufferedReader br = new BufferedReader(new InputStreamReader(
        fileInputStream));
      String configData = readFile(br);
      loadConfigs(configData);
    } else {
      throw new Exception("Can't find configFile=" + configFileName);
    }
  }