
    public static String getSecondaryServicePrincipalClientID(String envSecondaryServicePrincipal) throws Exception {
        Properties authSettings = new Properties();
        try (FileInputStream credentialsFileStream = new FileInputStream(new File(envSecondaryServicePrincipal))) {
            authSettings.load(credentialsFileStream);
        }

        return authSettings.getProperty("client");
    }