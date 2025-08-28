    public synchronized void load() {
        Properties entries = new Properties();
        File file = null;
        try {
            InputStream is;
            // First try to load the user-specific table, if it exists
            String userTablePath =
                System.getProperty("content.types.user.table");
            if (userTablePath != null) {
                file = new File(userTablePath);
                if (!file.exists()) {
                    // No user-table, try to load the default built-in table.
                    file = new File(System.getProperty("java.home") +
                                    File.separator +
                                    "lib" +
                                    File.separator +
                                    "content-types.properties");
                }
            }
            else {
                // No user table, try to load the default built-in table.
                file = new File(System.getProperty("java.home") +
                                File.separator +
                                "lib" +
                                File.separator +
                                "content-types.properties");
            }

            is = new BufferedInputStream(new FileInputStream(file));
            entries.load(is);
            is.close();
        }
        catch (IOException e) {
            System.err.println("Warning: default mime table not found: " +
                               file.getPath());
            return;
        }
        parse(entries);
    }
