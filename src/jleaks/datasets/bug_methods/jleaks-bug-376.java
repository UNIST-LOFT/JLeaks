  private void readXmlMapping() {

    try {
      ClassLoader classLoader = serverConfig.getClassLoadConfig().getClassLoader();

      Enumeration<URL> resources = classLoader.getResources("ebean.xml");

      List<XmEbean> mappings = new ArrayList<>();
      while (resources.hasMoreElements()) {
        URL url = resources.nextElement();
        InputStream is = url.openStream();
        mappings.add(XmlMappingReader.read(is));
        is.close();
      }

      for (XmEbean mapping : mappings) {
        List<XmEntity> entityDeploy = mapping.getEntity();
        for (XmEntity deploy : entityDeploy) {
          readEntityMapping(classLoader, deploy);
        }
      }

    } catch (IOException e) {
      throw new RuntimeException("Error reading ebean.xml", e);
    }
  }
