private static boolean keyspaceAlreadyExists(InetSocketAddress host, CassandraKeyValueServiceConfig config){
    try (CassandraClient client = CassandraClientFactory.getClientInternal(host, config)) {
        client.describe_keyspace(config.getKeyspaceOrThrow());
        CassandraKeyValueServices.waitForSchemaVersions(config, client, "while checking if schemas diverged on startup");
        return true;
    } catch (NotFoundException e) {
        return false;
    }
}