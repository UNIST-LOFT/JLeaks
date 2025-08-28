private void loadConfigsUsingClassLoader(String configFileName) throws Exception {
    BufferedInputStream fileInputStream = null;
    BufferedReader br = null;
    try {
      fileInputStream = (BufferedInputStream) this
        .getClass().getClassLoader()
        .getResourceAsStream(configFileName);
      if (fileInputStream != null) {
        br = new BufferedReader(new InputStreamReader(
          fileInputStream));
        String configData = readFile(br);
        loadConfigs(configData);
      } else {
        throw new Exception("Can't find configFile=" + configFileName);
      }
    } finally {
      if (br != null) {
        try {
          br.close();
        } catch (IOException e) {
        }
      }

      if (fileInputStream != null) {
        try {
          fileInputStream.close();
        } catch (IOException e) {
        }
      }
    }
  }