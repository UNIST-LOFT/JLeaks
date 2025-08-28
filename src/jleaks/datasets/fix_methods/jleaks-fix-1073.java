
    public SQLiteConnection(String url, String fileName, Properties prop) throws SQLException {
        DB newDB = null;
        try {
            this.db = newDB = open(url, fileName, prop);
            SQLiteConfig config = this.db.getConfig();
            this.connectionConfig = this.db.getConfig().newConnectionConfig();
            config.apply(this);
        } catch (Throwable t) {
            try {
                if (newDB != null) {
                    newDB.close();
                }
            } catch (Exception e) {
                t.addSuppressed(e);
            }
            throw t;
        }
    }