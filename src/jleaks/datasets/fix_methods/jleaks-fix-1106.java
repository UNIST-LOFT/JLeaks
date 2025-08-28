private File createExcludeFile(File testDir) throws IOException {
    File excludeFile = new File(testDir, "excludeFile");
    try (FileOutputStream out = new FileOutputStream(excludeFile)) {
      out.write("decommisssionedHost".getBytes(UTF_8));
    }
    return excludeFile;
  }