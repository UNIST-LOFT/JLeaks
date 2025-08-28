
  public static void load(MasterInfo info, String path) throws IOException {
    UnderFileSystem ufs = UnderFileSystem.get(path);
    DataInputStream imageIs = null;
    try {
      if (!ufs.exists(path)) {
        LOG.info("Image " + path + " does not exist.");
        return;
      }
      LOG.info("Loading image " + path);
      imageIs = new DataInputStream(ufs.open(path));
      JsonParser parser = JsonObject.createObjectMapper().getFactory().createParser(imageIs);
      info.loadImage(parser, path);
    } finally {
      if (imageIs != null) {
        imageIs.close();
      }
      ufs.close();
    }
  }
