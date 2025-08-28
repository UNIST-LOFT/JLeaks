private static File getCompressedAliasMap(File aliasMapDir)
      throws IOException {
    File outCompressedFile = new File(aliasMapDir.getParent(), TAR_NAME);

    try (BufferedOutputStream bOut = new BufferedOutputStream(
            Files.newOutputStream(outCompressedFile.toPath()));
         GzipCompressorOutputStream gzOut = new GzipCompressorOutputStream(bOut);
         TarArchiveOutputStream tOut = new TarArchiveOutputStream(gzOut)){

      addFileToTarGzRecursively(tOut, aliasMapDir, "", new Configuration());
    }

    return outCompressedFile;
  }