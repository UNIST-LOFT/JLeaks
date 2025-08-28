  private static void copyToZipStream(InputStream is, ZipEntry entry,
                              ZipOutputStream zos) throws IOException {
    zos.putNextEntry(entry);
    byte[] arr = new byte[4096];
    int read = is.read(arr);
    while (read > -1) {
      zos.write(arr, 0, read);
      read = is.read(arr);
    }
    is.close();
    zos.closeEntry();
  }
