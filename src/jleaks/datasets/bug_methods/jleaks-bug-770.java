	public void setTime(Date time) {
		FileWriter fileWriter = null;
		
		try {
			BufferedWriter writer;
			
			fileWriter = new FileWriter(newTimestampFile);
			writer = new BufferedWriter(fileWriter);
			
			writer.write(dateFormatter.format(time));
			
			writer.close();
			
			renameNewFileToCurrent();
			
		} catch (IOException e) {
			throw new OsmosisRuntimeException(
					"Unable to write the time to temporary file " + newTimestampFile + ".", e);
		} finally {
			if (fileWriter != null) {
				try {
					fileWriter.close();
				} catch (Exception e) {
					LOG.log(Level.WARNING, "Unable to close temporary time file " + newTimestampFile + ".", e);
				}
			}
		}
	}
