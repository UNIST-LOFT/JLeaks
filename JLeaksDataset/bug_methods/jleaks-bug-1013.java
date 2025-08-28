  public void insertAnnotations(String annotationFilePath, String javaFilePath) {
    try {
      File javaFile = new File(javaFilePath);
      String fileContents = FilesPlume.readFile(javaFile);
      String lineSeparator = FilesPlume.inferLineSeparator(annotationFilePath);
      FileInputStream annotationInputStream = new FileInputStream(annotationFilePath);
      String result = insertAnnotations(annotationInputStream, fileContents, lineSeparator);
      annotationInputStream.close();
      FilesPlume.writeFile(javaFile, result);
    } catch (IOException e) {
      System.err.println(
          "Failed to insert annotations from file "
              + annotationFilePath
              + " into file "
              + javaFilePath);
      System.exit(1);
    }
  }
