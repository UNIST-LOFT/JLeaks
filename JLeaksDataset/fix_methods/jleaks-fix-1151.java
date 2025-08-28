 public Connection beginTransaction(int isolationLevel){

        Connection connection = new Connection(this, false);

        boolean success = false;
        try {
            connection.getJdbcConnection().setAutoCommit(false);
            connection.getJdbcConnection().setTransactionIsolation(isolationLevel);
            success = true;
        } catch (SQLException e) {
            throw new Sql2oException("Could not start the transaction - " + e.getMessage(), e);
        } finally {
            if (!success) {
                connection.close();
            }
        }

        return connection;
    }