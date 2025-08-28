public synchronized void load() 
{
    Properties entries = new Properties();
    File file = null;
    InputStream in;
    // First try to load the user-specific table, if it exists
    String userTablePath = System.getProperty("content.types.user.table");
    if (userTablePath != null && (file = new File(userTablePath)).exists()) {
        try {
            in = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            System.err.println("Warning: " + file.getPath() + " mime table not found.");
            return;
        }
    } else {
        in = MimeTable.class.getResourceAsStream("content-types.properties");
        if (in == null)
            throw new InternalError("default mime table not found");
    }
    try (BufferedInputStream bin = new BufferedInputStream(in)) {
        entries.load(bin);
    } catch (IOException e) {
        System.err.println("Warning: " + e.getMessage());
    }
    parse(entries);
}