public static File compressFiles(Collection<File> files, File output, String bundleRoot){
    Logger.info(PushUtils.class, "Compressing " + files.size() + " to " + output.getAbsoluteFile());
    // Create the output stream for the output file
    // try-with-resources handles close of streams
    try (OutputStream fos = Files.newOutputStream(output.toPath());
        // Wrap the output file stream in streams that will tar and gzip everything
        TarArchiveOutputStream taos = new TarArchiveOutputStream(new GZIPOutputStream(new BufferedOutputStream(fos)))) {
        // TAR originally didn't support long file names, so enable the support for it
        taos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
        // Get to putting all the files in the compressed output file
        for (File f : files) {
            addFilesToCompression(taos, f, ".", bundleRoot);
        }
    }
    return output;
}