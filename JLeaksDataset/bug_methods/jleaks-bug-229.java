  public File createAppLoaderImportZip(String importPath) {

    importPath = importPath.replaceAll("/|\\\\", "(/|\\\\\\\\)");
    List<URL> fileUrls = MetaScanner.findAll(importPath);

    if (fileUrls.isEmpty()) {
      return null;
    }

    try {
      File zipFile = MetaFiles.createTempFile("app-", ".zip").toFile();
      ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFile));
      for (URL url : fileUrls) {
        File file = new File(url.getFile());
        ZipEntry zipEntry = new ZipEntry(file.getName());
        zipOutputStream.putNextEntry(zipEntry);
        IOUtils.copy(url.openStream(), zipOutputStream);
      }
      zipOutputStream.close();

      return zipFile;
    } catch (IOException e) {
      e.printStackTrace();
    }

    return null;
  }
