private static String searchForInternetExplorerVersion (String userAgent) 
{
    String line = null;
    String UserAgentListFile = "xml/internet-explorer-user-agents.txt";
    String browserVersion = "";
    BufferedReader reader = null;
    try {
        reader = new BufferedReader(new FileReader(UserAgentListFile));
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("#")) {
                browserVersion = line.substring(2, line.length() - 1);
                continue;
            }
            if (line.toLowerCase().equals(userAgent.toLowerCase())) {
                return browserVersion;
            }
        }
        reader.close();
    } catch (IOException e) {
        logger.debug("Error on opening/reading IE user agent file. Error:" + e.getMessage());
    } finally {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                logger.debug("Error on closing reader file. Error:" + e.getMessage());
            }
        }
    }
    return "-1";
}