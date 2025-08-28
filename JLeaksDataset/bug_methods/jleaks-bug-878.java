  private boolean isExtractedLibUptodate(File extractedLib) {
    if (extractedLib.exists()) {
      try {
        String existingMd5 = md5sum(new FileInputStream(extractedLib));
        String actualMd5 = md5sum(getLibraryStream());
        return existingMd5.equals(actualMd5);
      } catch (IOException e) {
        return false;
      }
    } else {
      return false;
    }
  }
