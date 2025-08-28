  private void createExcludeFile(String filename) throws IOException {
    File file = new File(filename);
    if (file.exists()) {
      file.delete();
    }

    FileOutputStream out = new FileOutputStream(file);
    out.write("decommisssionedHost".getBytes());
    out.close();
  }
