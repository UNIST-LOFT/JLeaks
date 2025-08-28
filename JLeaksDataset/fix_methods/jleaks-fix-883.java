private void restoreFiles(List<ZipEntry> uploadFiles) 
{
    String appPath = Common.ctx.getServletContext().getRealPath(FILE_SEPARATOR);
    for (ZipEntry zipEntry : uploadFiles) {
        String entryName = zipEntry.getName();
        File file = new File(appPath + entryName);
        writeToFile(zipEntry, file);
    }
}