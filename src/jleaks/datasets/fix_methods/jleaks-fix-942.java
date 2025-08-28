private void sendFileToRemoteDirectory(File file, String remoteDirectory, String fileName, Session session){
    FileInputStream fileInputStream = new FileInputStream(file);
    if (!StringUtils.hasText(remoteDirectory)) {
        remoteDirectory = "";
    } else if (!remoteDirectory.endsWith(remoteFileSeparator)) {
        remoteDirectory += remoteFileSeparator;
    }
    String remoteFilePath = remoteDirectory + fileName;
    // write remote file first with .writing extension
    String tempFilePath = remoteFilePath + this.temporaryFileSuffix;
    if (this.autoCreateDirectory) {
        this.ensureDirectoryExists(session, remoteDirectory, remoteDirectory);
    }
    try {
        session.write(fileInputStream, tempFilePath);
        // then rename it to its final name
        session.rename(tempFilePath, remoteFilePath);
    } catch (Exception e) {
        throw new MessagingException("Failed to write to '" + tempFilePath + "' while uploading the file", e);
    } finally {
        fileInputStream.close();
    }
}