protected static List<String> getDatabasesHelper(final IMetaStoreClient mClient) throws TException 
{
    try {
        return mClient.getAllDatabases();
    } catch (MetaException e) {
        throw e;
    } catch (TException e) {
        logger.warn("Failure while attempting to get hive databases. Retries once.", e);
        try {
            mClient.close();
        } catch (Exception ex) {
            logger.warn("Failure while attempting to close existing hive metastore connection. May leak connection.", ex);
        }
        mClient.reconnect();
        return mClient.getAllDatabases();
    }
}