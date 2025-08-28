private Date getServerTimestamp(URL baseUrl)
{
    URL timestampUrl;
    try {
        timestampUrl = new URL(baseUrl, SERVER_TSTAMP_FILE);
    } catch (MalformedURLException e) {
        throw new OsmosisRuntimeException("The server timestamp URL could not be created.", e);
    }
    try {
        Date result;
        URLConnection connection = timestampUrl.openConnection();
        // timeout 15 minutes
        connection.setReadTimeout(15 * 60 * 1000);
        // timeout 15 minutes
        connection.setConnectTimeout(15 * 60 * 1000);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            result = dateParser.parse(reader.readLine());
        }
        return result;
    } catch (IOException e) {
        throw new OsmosisRuntimeException("Unable to read the timestamp from the server.", e);
    }
}