    private List<String> getChangeLogs(LiquibaseMongodbBuildTimeConfig liquibaseBuildConfig) {
        ChangeLogParameters changeLogParameters = new ChangeLogParameters();
        ClassLoaderResourceAccessor classLoaderResourceAccessor = new ClassLoaderResourceAccessor(
                Thread.currentThread().getContextClassLoader());

        ChangeLogParserFactory changeLogParserFactory = ChangeLogParserFactory.getInstance();

        Set<String> resources = new LinkedHashSet<>();

        resources.addAll(findAllChangeLogFiles(liquibaseBuildConfig.changeLog, changeLogParserFactory,
                classLoaderResourceAccessor, changeLogParameters));

        LOGGER.debugf("Liquibase changeLogs: %s", resources);

        return new ArrayList<>(resources);
    }
