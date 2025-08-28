  private void serializeDeviceOwnerMap(File deviceOwnerFile) throws IOException {
    if (!deviceOwnerFile.getParentFile().exists()) {
      deviceOwnerFile.getParentFile().mkdirs();
    }
    if (!deviceOwnerFile.exists()) {
      deviceOwnerFile.createNewFile();
    }
    try (FileOutputStream fos = new FileOutputStream(deviceOwnerFile, false);
        ObjectOutputStream deviceOwnerOutput = new ObjectOutputStream(fos)) {
      deviceOwnerOutput.writeObject(deviceOwnerMap);
    }
  }
