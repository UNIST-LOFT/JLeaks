private List<String> getChangeLogs(LiquibaseMongodbBuildTimeConfig liquibaseBuildConfig) 
{
    ChangeLogParameters changeLogParameters = new ChangeLogParameters();
    ChangeLogParserFactory changeLogParserFactory = ChangeLogParserFactory.getInstance();
    Set<String> resources = new LinkedHashSet<>();
    ClassLoaderResourceAccessor classLoaderResourceAccessor = new ClassLoaderResourceAccessor(Thread.currentThread().getContextClassLoader());
    try {
        resources.addAll(findAllChangeLogFiles(liquibaseBuildConfig.changeLog, changeLogParserFactory, classLoaderResourceAccessor, changeLogParameters));
        LOGGER.debugf("Liquibase changeLogs: %s", resources);
        return new ArrayList<>(resources);
    } finally {
        try {
            classLoaderResourceAccessor.close();
        } catch (Exception ignored) {
            // close() really shouldn't declare that exception, see also https://github.com/liquibase/liquibase/pull/2576
        }
    }
}