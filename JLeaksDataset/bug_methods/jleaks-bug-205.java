  private void serializeDeviceOwnerMap(File deviceOwnerFile) throws IOException {
    if (!deviceOwnerFile.getParentFile().exists()) {
      deviceOwnerFile.getParentFile().mkdirs();
    }
    if (!deviceOwnerFile.exists()) {
      deviceOwnerFile.createNewFile();
    }
    try (ObjectOutputStream deviceOwnerOutput = new ObjectOutputStream(
        new FileOutputStream(deviceOwnerFile, false))) {
      deviceOwnerOutput.writeObject(deviceOwnerMap);
    }
  }
