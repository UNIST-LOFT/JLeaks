  public static Properties getAssetInfo(MD5Key id) {

    File infoFile = getAssetInfoFile(id);
    try {

      Properties props = new Properties();
      InputStream is = new FileInputStream(infoFile);
      props.load(is);
      is.close();
      return props;

    } catch (Exception e) {
      return new Properties();
    }
  }
