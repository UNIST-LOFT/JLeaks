	public void readyToSend(ResponseSender sender) throws Exception
	{
		addStandardHeaders();
		sender.send(makeHttpHeaders().getBytes());
		while(!reader.isEof())
			sender.send(reader.readBytes(1000));
		reader.close();
		sender.close();
	}
