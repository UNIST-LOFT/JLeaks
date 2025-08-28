	private void sendFileToRemoteDirectory(File file, String remoteDirectory, String fileName, Session session) 
			throws FileNotFoundException, IOException {
		
		FileInputStream fileInputStream = new FileInputStream(file);
		if (!StringUtils.hasText(remoteDirectory)) {
			remoteDirectory = "";
		}
		else if (!remoteDirectory.endsWith(remoteFileSeparator)) {
			remoteDirectory += remoteFileSeparator; 
		}
		String remoteFilePath = remoteDirectory + fileName;
		// write remote file first with .writing extension
		String tempFilePath = remoteFilePath + this.temporaryFileSuffix;
		
		if (this.autoCreateDirectory){
			this.ensureDirectoryExists(session, remoteDirectory, remoteDirectory);
		}
		
		session.write(fileInputStream, tempFilePath);
		fileInputStream.close();
		// then rename it to its final name
		session.rename(tempFilePath, remoteFilePath);
	}
