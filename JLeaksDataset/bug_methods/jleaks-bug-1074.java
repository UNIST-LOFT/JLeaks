    public SQLiteConnection(String url, String fileName, Properties prop) throws SQLException {
        this.db = open(url, fileName, prop);
        SQLiteConfig config = db.getConfig();
        this.connectionConfig = db.getConfig().newConnectionConfig();

        config.apply(this);
    }
