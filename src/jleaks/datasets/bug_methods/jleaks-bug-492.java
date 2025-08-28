	public SessionFactory buildSessionFactory() throws HibernateException {
		log.debug( "Building session factory using internal StandardServiceRegistryBuilder" );
		standardServiceRegistryBuilder.applySettings( properties );
		return buildSessionFactory( standardServiceRegistryBuilder.build() );
	}
