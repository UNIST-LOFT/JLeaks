    public static void runScript(String scriptFile) {
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
            Connection connection = DriverManager.getConnection("jdbc:derby:mirthdb;create=true");
            InputStream in = new FileInputStream(new File(scriptFile));
            OutputStream out = new NullOutputStream();
            ij.runScript(connection, in, "UTF-8", out, "UTF-8");
        } catch (Exception e) {
            logger.error("error executing script", e);
        }
    }
