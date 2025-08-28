	protected EmbeddedCacheManager createCacheManager(
			final Properties properties,
			final ServiceRegistry serviceRegistry) throws CacheException {
		final String configLoc = ConfigurationHelper.getString(
				INFINISPAN_CONFIG_RESOURCE_PROP,
				properties,
				DEF_INFINISPAN_CONFIG_RESOURCE
		);
		final FileLookup fileLookup = FileLookupFactory.newInstance();
		//The classloader of the current module:
		final ClassLoader infinispanClassLoader = InfinispanRegionFactory.class.getClassLoader();

		return serviceRegistry.getService( ClassLoaderService.class ).workWithClassLoader(
				new ClassLoaderService.Work<EmbeddedCacheManager>() {
					@Override
					public EmbeddedCacheManager doWork(ClassLoader classLoader) {
						try {
							InputStream is;
							is = fileLookup.lookupFile( configLoc, classLoader );
							if ( is == null ) {
								// when it's not a user-provided configuration file, it might be a default configuration file,
								// and if that's included in [this] module might not be visible to the ClassLoaderService:
								classLoader = infinispanClassLoader;
								// This time use lookupFile*Strict* so to provide an exception if we can't find it yet:
								is = FileLookupFactory.newInstance().lookupFileStrict( configLoc, classLoader );
							}
							final ParserRegistry parserRegistry = new ParserRegistry( infinispanClassLoader );
							final ConfigurationBuilderHolder holder = parseWithOverridenClassLoader( parserRegistry, is, infinispanClassLoader );

							// Override global jmx statistics exposure
							final String globalStats = extractProperty(
									INFINISPAN_GLOBAL_STATISTICS_PROP,
									properties
							);
							if ( globalStats != null ) {
								holder.getGlobalConfigurationBuilder()
										.globalJmxStatistics()
										.enabled( Boolean.parseBoolean( globalStats ) );
							}

							return createCacheManager( holder );
						}
						catch (IOException e) {
							throw new CacheException( "Unable to create default cache manager", e );
						}
					}

				}
		);
	}
