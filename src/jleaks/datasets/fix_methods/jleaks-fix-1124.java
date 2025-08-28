public static void downloadFromMaster(Map conf, String file, String localFile) throws AuthorizationException, IOException, TException {
        NimbusClient client = NimbusClient.getConfiguredClient(conf);
        try {
        	download(client, file, localFile);
        } finally {
        	client.close();
        }
    }