    private void lazyInit() {
        if (emf == null) {
            synchronized (this) {
                if (emf == null) {
                    this.emf = createEntityManagerFactory();
                    JpaMapUtils.addSpecificNamedQueries(emf);

                    // consistency check for transaction handling, as this would lead to data-inconsistencies as changes wouldn't commit when expected
                    if (jtaEnabled && !this.emf.getProperties().get(AvailableSettings.JPA_TRANSACTION_TYPE).equals(PersistenceUnitTransactionType.JTA.name())) {
                        throw new ModelException("Consistency check failed: If Keycloak is run with JTA, the Entity Manager for JPA map storage should be run with JTA as well.");
                    }

                    // consistency check for auto-commit, as this would lead to data-inconsistencies as changes wouldn't roll back when expected
                    EntityManager em = getEntityManager();
                    em.unwrap(SessionImpl.class).doWork(connection -> {
                        if (connection.getAutoCommit()) {
                            throw new ModelException("The database connection must not use auto-commit. For Quarkus, auto-commit was off once JTA was enabled for the EntityManager.");
                        }
                    });
                    em.close();
                }
            }
        }
    }
