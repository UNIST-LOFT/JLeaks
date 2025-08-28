void shutdown(boolean compact) throws SQLException 
{
    super.shutdown(compact);
    try (Connection conn = getNewConnection()) {
        String statement;
        if (compact) {
            // db is not new and useful for future.  Compact it.
            statement = "SHUTDOWN COMPACT";
        } else {
            // new need to compact database.  just shutdown.
            statement = "SHUTDOWN";
        }
        try (CallableStatement psCompact = conn.prepareCall(statement)) {
            psCompact.execute();
        }
    }
}