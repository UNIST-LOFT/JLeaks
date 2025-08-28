	private static String searchForInternetExplorerVersion (String userAgent) {
		String line = null;
		String UserAgentListFile = "xml/internet-explorer-user-agents.txt";
		String browserVersion = "";
		try {
			BufferedReader reader = new BufferedReader(new FileReader(UserAgentListFile));
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("#")) {
					browserVersion = line.substring(2, line.length()-1);
					continue;
				}
				if (line.toLowerCase().equals(userAgent.toLowerCase())) {
					reader.close();
					return browserVersion;
				}
			}
			reader.close();
		} catch (IOException e) {
			logger.debug("Error on opening/reading IE user agent file. Error:" + e.getMessage());
		}
		return "-1";
	}
