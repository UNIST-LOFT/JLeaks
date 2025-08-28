    public static void checkIndexExistence(Settings settings, RestRepository client) {
        // check index existence
        if (!settings.getIndexAutoCreate()) {
            if (client == null) {
                client = new RestRepository(settings);
            }
            if (!client.indexExists(false)) {
                client.close();
                throw new EsHadoopIllegalArgumentException(String.format("Target index [%s] does not exist and auto-creation is disabled [setting '%s' is '%s']",
                        settings.getResourceWrite(), ConfigurationOptions.ES_INDEX_AUTO_CREATE, settings.getIndexAutoCreate()));
            }
        }
    }
