public static VersionChecker getInstance() throws Exception 
{
    VersionChecker instance;
    // Input stream on the remote XML file.
    InputStream in;
    AppLogger.fine("Opening connection to " + RuntimeConstants.VERSION_URL);
    // Parses the remote XML file using UTF-8 encoding.
    in = FileFactory.getFile(RuntimeConstants.VERSION_URL).getInputStream();
    try {
        SAXParserFactory.newInstance().newSAXParser().parse(in, instance = new VersionChecker());
    } catch (Exception e) {
        AppLogger.fine("Failed to read version XML file at " + RuntimeConstants.VERSION_URL, e);
        throw e;
    } finally {
        in.close();
    }
    // Makes sure we retrieved the information we were looking for.
    // We're not checking the release date as older version of muCommander
    // didn't use it.
    if (instance.latestVersion == null || instance.latestVersion.equals("") || instance.downloadURL == null || instance.downloadURL.equals(""))
        throw new Exception();
    return instance;
}