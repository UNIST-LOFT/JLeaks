    public static void downloadFromMaster(Map conf, String file, String localFile) throws AuthorizationException, IOException, TException {
        NimbusClient client = NimbusClient.getConfiguredClient(conf);
        download(client, file, localFile);
    }
