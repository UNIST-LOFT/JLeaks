  private static File getCompressedAliasMap(File aliasMapDir)
      throws IOException {
    File outCompressedFile = new File(aliasMapDir.getParent(), TAR_NAME);
    BufferedOutputStream bOut = null;
    GzipCompressorOutputStream gzOut = null;
    TarArchiveOutputStream tOut = null;
    try {
      bOut = new BufferedOutputStream(
          Files.newOutputStream(outCompressedFile.toPath()));
      gzOut = new GzipCompressorOutputStream(bOut);
      tOut = new TarArchiveOutputStream(gzOut);
      addFileToTarGzRecursively(tOut, aliasMapDir, "", new Configuration());
    } finally {
      if (tOut != null) {
        tOut.finish();
      }
      IOUtils.cleanupWithLogger(null, tOut, gzOut, bOut);
    }
    return outCompressedFile;
  }
