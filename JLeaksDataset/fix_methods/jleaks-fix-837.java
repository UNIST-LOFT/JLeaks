    public Map<String, String> getProperties() {
        JDBCConfigSourceHelper helper = getHelper();
        try {
            return helper.getAllConfigValues();
        } finally {
            try {
                helper.close();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error closing JDBC connection", e);
            }
        }
    }
