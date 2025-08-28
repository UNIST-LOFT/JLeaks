  protected void unZip(File file, File directory) throws IOException {

    File extractFile = null;
    FileOutputStream fileOutputStream = null;
    try (ZipFile zipFile = new ZipFile(file); ) {
      Enumeration<? extends ZipEntry> entries = zipFile.entries();

      while (entries.hasMoreElements()) {
        ZipEntry entry = entries.nextElement();
        try (InputStream entryInputStream = zipFile.getInputStream(entry)) {
          byte[] buffer = new byte[1024];
          int bytesRead = 0;

          extractFile = new File(directory, entry.getName());
          if (entry.isDirectory()) {
            extractFile.mkdirs();
            continue;
          } else {
            extractFile.getParentFile().mkdirs();
            extractFile.createNewFile();
          }

          fileOutputStream = new FileOutputStream(extractFile);
          while ((bytesRead = entryInputStream.read(buffer)) != -1) {
            fileOutputStream.write(buffer, 0, bytesRead);
          }

          if (Files.getFileExtension(extractFile.getName()).equals("xlsx")) {
            importExcel(extractFile);
          }
        } catch (IOException ioException) {
          log.error(ioException.getMessage());
          continue;
        }
      }
    }
  }
