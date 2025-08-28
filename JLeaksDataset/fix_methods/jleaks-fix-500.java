	private static StandardServiceRegistry buildStandardServiceRegistry(CommandLineArgs commandLineArgs)
			throws Exception {
		final BootstrapServiceRegistry bsr = new BootstrapServiceRegistryBuilder().build();
		final StandardServiceRegistryBuilder ssrBuilder = new StandardServiceRegistryBuilder( bsr );

		if ( commandLineArgs.cfgXmlFile != null ) {
			ssrBuilder.configure( commandLineArgs.cfgXmlFile );
		}

		Properties properties = new Properties();
		if ( commandLineArgs.propertiesFile != null ) {
			try ( final FileInputStream fis = new FileInputStream( commandLineArgs.propertiesFile ) ) {
				properties.load( fis );
			}
		}
		ssrBuilder.applySettings( properties );

		return ssrBuilder.build();
	}
