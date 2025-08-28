  protected void copyFile(File sourceFile, File targetFile) throws IOException {
    copyFile(new FileInputStream(sourceFile), targetFile);
  }
