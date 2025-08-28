    void shutdown(boolean compact) throws SQLException {
        super.shutdown(compact);
        Connection conn = getNewConnection();
        CallableStatement psCompact = null;

        if (compact) {
            // db is not new and useful for future.  Compact it.
            psCompact = conn.prepareCall("SHUTDOWN COMPACT");

        } else {
            // new need to compact database.  just shutdown.
            psCompact = conn.prepareCall("SHUTDOWN");
        }

        psCompact.execute();
        psCompact.close();
        conn.close();
    }
