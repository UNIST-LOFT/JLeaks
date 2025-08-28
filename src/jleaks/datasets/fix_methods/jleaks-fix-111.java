private static void copyToZipStream(File file, ZipEntry entry,
                              ZipOutputStream zos) throws IOException {
    InputStream is = new FileInputStream(file);
    try {
      zos.putNextEntry(entry);
      byte[] arr = new byte[4096];
      int read = is.read(arr);
      while (read > -1) {
        zos.write(arr, 0, read);
        read = is.read(arr);
      }
    } finally {
      try {
        is.close();
      } finally {
        zos.closeEntry();
      }
    }
  }