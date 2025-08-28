    public static VersionChecker getInstance() throws Exception {
        URLConnection  conn;   // Connection to the remote XML file.
        InputStream    in;     // Input stream on the remote XML file.
        VersionChecker instance;

        AppLogger.fine("Opening connection to " + RuntimeConstants.VERSION_URL);

        // Initialisation.
        conn   = new URL(RuntimeConstants.VERSION_URL).openConnection();
        conn.setRequestProperty("user-agent", PlatformManager.USER_AGENT);

        // Parses the remote XML file using UTF-8 encoding.
        conn.connect();
        in = conn.getInputStream();
        SAXParserFactory.newInstance().newSAXParser().parse(in, instance = new VersionChecker());
        in.close();

        // Makes sure we retrieved the information we were looking for.
        // We're not checking the release date as older version of muCommander
        // didn't use it.
        if(instance.latestVersion == null || instance.latestVersion.equals("") ||
           instance.downloadURL == null   || instance.downloadURL.equals(""))
            throw new Exception();

        return instance;
    }
