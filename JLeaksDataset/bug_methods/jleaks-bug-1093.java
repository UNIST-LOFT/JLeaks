	public static void createFileWithContent(String filePath, String content)
	{
		File file = new File(filePath);
		try
		{
			file.createNewFile();
			FileWriter fw = new FileWriter(file);
			fw.write(content);
			fw.close();
		}
		catch (IOException e)
		{
			LOGGER.error(e.getMessage());
		}
	}
