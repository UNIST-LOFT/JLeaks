private static void extract(final ZipFile zipFile, final ZipEntry zipEntry, final File toDir) throws IOException 
{
    final File outputDir = new File(outputDirStr);
    outputDir.mkdirs();
    ZipEntry ze = null;
    try (ZipInputStream zin = new ZipInputStream(zipFile)) {
        while ((ze = zin.getNextEntry()) != null) {
            Logger.info(ZipUtil.class, "Unzipping " + ze.getName());
            final File newFile = new File(outputDir + File.separator + ze.getName());
            if (newFile.getCanonicalPath().startsWith(outputDirStr)) {
                try (OutputStream os = Files.newOutputStream(newFile.toPath())) {
                    IOUtils.copy(zin, os);
                }
            } else {
                // in case of an invalid attempt this will report the exception
                checkSecurity(outputDir, newFile);
            }
        }
    } catch (final IOException e) {
        final String errorMsg = String.format("Error while unzipping Data in file '%s': %s", null != ze ? ze.getName() : "", e.getMessage());
        Logger.error(ZipUtil.class, errorMsg, e);
        throw e;
    }
}