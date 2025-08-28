public void close() throws DatabaseException 
{
    Scope.getCurrentScope().getSingleton(ExecutorService.class).clearExecutor("jdbc", this);
    try (final DatabaseConnection connection = getConnection()) {
        if (connection != null && previousAutoCommit != null) {
            connection.setAutoCommit(previousAutoCommit);
        }
    } catch (final DatabaseException e) {
        Scope.getCurrentScope().getLog(getClass()).warning("Failed to restore the auto commit to " + previousAutoCommit);
        throw e;
    }
}