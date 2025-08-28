    public void close() throws DatabaseException {
        Scope.getCurrentScope().getSingleton(ExecutorService.class).clearExecutor("jdbc", this);
        DatabaseConnection connection = getConnection();
        if (connection != null) {
            if (previousAutoCommit != null) {
                try {
                    connection.setAutoCommit(previousAutoCommit);
                } catch (DatabaseException e) {
                    Scope.getCurrentScope().getLog(getClass()).warning("Failed to restore the auto commit to " + previousAutoCommit);

                    throw e;
                }
            }
            connection.close();
        }
    }
