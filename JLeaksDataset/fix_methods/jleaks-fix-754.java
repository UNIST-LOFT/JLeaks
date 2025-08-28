public void compressFiles(List<File> listFiles, String destZipFile)
throws IOException {

    try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(destZipFile))) {
        for (File file : listFiles) {
            if (file.isDirectory()) {
                addFolderToZip(file, file.getName(), zos);
            } else {
                addFileToZip(file, zos);
            }
        }
        zos.flush();
    }
}