  public static void create(MasterInfo info, String path) throws IOException {
    String tPath = path + ".tmp";
    String parentFolder = path.substring(0, path.lastIndexOf(Constants.PATH_SEPARATOR));
    LOG.info("Creating the image file: " + tPath);
    UnderFileSystem ufs = UnderFileSystem.get(path);
    if (!ufs.exists(parentFolder)) {
      LOG.info("Creating parent folder " + parentFolder);
      ufs.mkdirs(parentFolder, true);
    }
    OutputStream os = ufs.create(tPath);
    DataOutputStream imageOs = new DataOutputStream(os);
    ObjectWriter writer = JsonObject.createObjectMapper().writer();

    info.writeImage(writer, imageOs);
    imageOs.flush();
    imageOs.close();

    LOG.info("Succefully created the image file: " + tPath);
    ufs.delete(path, false);
    ufs.rename(tPath, path);
    ufs.delete(tPath, false);
    LOG.info("Renamed " + tPath + " to " + path);
    // safe to close, nothing created here with scope outside function
    ufs.close();
  }
