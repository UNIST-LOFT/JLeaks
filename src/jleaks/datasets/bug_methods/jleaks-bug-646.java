    protected void close(KeycloakSession session) {
        KeycloakTransactionManager tx = session.getTransactionManager();
        if (tx.isActive()) {
            if (tx.getRollbackOnly()) {
                tx.rollback();
            } else {
                tx.commit();
            }
        }
        session.close();
    }
