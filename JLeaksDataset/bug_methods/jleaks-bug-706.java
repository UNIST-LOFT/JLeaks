	private static void readBlacklist() throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(boxBlacklistFile));
		int entries = 0;

		String readingLine;
		int line = 0;

		while((readingLine = reader.readLine()) != null)
		{
			line++;

			if(readingLine.startsWith("#") || readingLine.trim().isEmpty())
			{
				continue;
			}

			String[] split = readingLine.split(" ");

			if(split.length != 2 || !isInteger(split[split.length-1]))
			{
				Mekanism.logger.error("BoxBlacklist.txt: Couldn't parse blacklist data on line " + line);
				continue;
			}
			
			String blockName = split[0].trim();
			
			Block block = Block.getBlockFromName(blockName);
			
			if(block == null)
			{
				Mekanism.logger.error("BoxBlacklist.txt: Couldn't find specified block on line " + line);
				continue;
			}

			MekanismAPI.addBoxBlacklist(block, Integer.parseInt(split[split.length-1]));
			entries++;
		}

		reader.close();

		Mekanism.logger.info("Finished loading Cardboard Box blacklist (loaded " + entries + " entries)");
	}
