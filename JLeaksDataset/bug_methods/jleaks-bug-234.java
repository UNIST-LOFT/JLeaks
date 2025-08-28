    public static String getSecondaryServicePrincipalClientID(String envSecondaryServicePrincipal) throws Exception {
        Properties authSettings = new Properties();
        FileInputStream credentialsFileStream = new FileInputStream(new File(envSecondaryServicePrincipal));
        authSettings.load(credentialsFileStream);
        credentialsFileStream.close();

        return authSettings.getProperty("client");
    }