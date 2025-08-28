  public static void load(MasterInfo info, String path) throws IOException {
    UnderFileSystem ufs = UnderFileSystem.get(path);
    if (!ufs.exists(path)) {
      LOG.info("Image " + path + " does not exist.");
      return;
    }
    LOG.info("Loading image " + path);
    DataInputStream imageIs = new DataInputStream(ufs.open(path));
    JsonParser parser = JsonObject.createObjectMapper().getFactory().createParser(imageIs);

    info.loadImage(parser, path);
    imageIs.close();
    ufs.close();
  }
