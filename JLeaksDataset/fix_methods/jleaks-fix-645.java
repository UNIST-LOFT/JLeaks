private void lazyInit() 
{
    /*
         On Quarkus, and EMF can be created even when the database is currently not available.
         Closing the EMF is not an option, as it is managed by Quarkus.
         Therefore, try to initialize it as often as needed, especially for the addSpecificNamedQueries()
         which would cause failures later if not initialized here.
        */
    if (!initialized) {
        synchronized (this) {
            if (emf == null) {
                this.emf = createEntityManagerFactory();
            }
            if (!initialized) {
                JpaMapUtils.addSpecificNamedQueries(emf);
                // consistency check for transaction handling, as this would lead to data-inconsistencies as changes wouldn't commit when expected
                if (jtaEnabled && !this.emf.getProperties().get(AvailableSettings.JPA_TRANSACTION_TYPE).equals(PersistenceUnitTransactionType.JTA.name())) {
                    throw new ModelException("Consistency check failed: If Keycloak is run with JTA, the Entity Manager for JPA map storage should be run with JTA as well.");
                }
                // consistency check for auto-commit, as this would lead to data-inconsistencies as changes wouldn't roll back when expected
                EntityManager em = getEntityManager();
                try {
                    em.unwrap(SessionImpl.class).doWork(connection -> {
                        if (connection.getAutoCommit()) {
                            throw new ModelException("The database connection must not use auto-commit. For Quarkus, auto-commit was off once JTA was enabled for the EntityManager.");
                        }
                    });
                } finally {
                    em.close();
                }
                initialized = true;
            }
        }
    }
}