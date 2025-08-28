public static void checkIndexExistence(Settings settings, RestRepository client) 
{
    // Only open a connection and check if autocreate is disabled
    if (!settings.getIndexAutoCreate()) {
        RestRepository repository = new RestRepository(settings);
        try {
            doCheckIndexExistence(settings, repository);
        } finally {
            repository.close();
        }
    }
}