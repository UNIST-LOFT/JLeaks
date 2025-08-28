    public synchronized void close() {
        for (Connection conn : allConnections) {
            try {
                conn.rollback();
                conn.close();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
        allConnections.clear();
    }
