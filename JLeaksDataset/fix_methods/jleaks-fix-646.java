protected void close(KeycloakSession session) 
{
    KeycloakTransactionManager tx = session.getTransactionManager();
    try {
        if (tx.isActive()) {
            if (tx.getRollbackOnly()) {
                tx.rollback();
            } else {
                tx.commit();
            }
        }
    } finally {
        session.close();
    }
}