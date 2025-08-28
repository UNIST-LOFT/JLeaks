public SessionFactory buildSessionFactory() throws HibernateException 
{
    log.debug("Building session factory using internal StandardServiceRegistryBuilder");
    standardServiceRegistryBuilder.applySettings(properties);
    StandardServiceRegistry serviceRegistry = standardServiceRegistryBuilder.build();
    try {
        return buildSessionFactory(serviceRegistry);
    } catch (Throwable t) {
        serviceRegistry.close();
        throw t;
    }
}