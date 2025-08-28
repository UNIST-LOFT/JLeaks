  public MetaFile downloadZip(String downloadFileName) throws Exception {

    ZipFile zipFile;
    File downloadFile = null;
    File cityTextFile = null;
    FileWriter writer;
    File tempDir = null;
    MetaFile metaFile = null;

    try {
      tempDir = Files.createTempDir();
      downloadFile = new File(tempDir, downloadFileName);

      cityTextFile = new File(tempDir, CITYTEXTFILE);

      URL url = new URL(DOWNLOAD_LINK + downloadFileName);

      FileUtils.copyURLToFile(url, downloadFile);

      LOG.debug("path for downloaded zip file : " + downloadFile.getPath());
      zipFile = new ZipFile(downloadFile.getPath());

      Enumeration<? extends ZipEntry> entries = zipFile.entries();

      while (entries.hasMoreElements()) {
        ZipEntry entry = entries.nextElement();

        if (entry.getName().equals(downloadFileName.replace("zip", "txt"))) {
          BufferedReader stream =
              new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));

          String line;
          StringBuffer buffer = new StringBuffer();

          while ((line = stream.readLine()) != null) {
            buffer.append(line + "\n");
          }

          cityTextFile.createNewFile();

          writer = new FileWriter(cityTextFile);

          writer.flush();
          writer.write(buffer.toString().replace("\"", ""));
          writer.close();

          LOG.debug("Length of file : " + cityTextFile.length());
          break;
        }
      }
      zipFile.close();

      metaFile = metaFiles.upload(cityTextFile);
      FileUtils.forceDelete(tempDir);

    } catch (UnknownHostException hostExp) {
      throw new Exception(I18n.get(IExceptionMessage.SERVER_CONNECTION_ERROR), hostExp);
    } catch (Exception e) {
      throw e;
    }

    return metaFile;
  }
