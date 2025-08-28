  public static void writeBufferToFile(String path, byte[] buffer) throws IOException {
    FileOutputStream os = new FileOutputStream(path);
    os.write(buffer);
    os.close();
  }
