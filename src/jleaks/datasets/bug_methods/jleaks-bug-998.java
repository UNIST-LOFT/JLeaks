  private void saveCenters() {
    try {
      final String fileName =
          new FileSave("Where To Save centers.txt ?", "centers.txt", mapFolderLocation).getPathString();
      if (fileName == null) {
        return;
      }
      final FileOutputStream out = new FileOutputStream(fileName);
      PointFileReaderWriter.writeOneToOne(out, centers);
      out.flush();
      out.close();
      System.out.println("Data written to :" + new File(fileName).getCanonicalPath());
    } catch (final Exception ex) {
      ClientLogger.logQuietly(ex);
    }
  }
