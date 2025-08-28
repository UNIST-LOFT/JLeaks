    public void close() {
        boolean connectionIsClosed = false;
        try {
            connectionIsClosed = this.getJdbcConnection().isClosed();
        } catch (SQLException e) {
            throw new Sql2oException("Sql2o encountered a problem while trying to determine whether the connection is closed.", e);
        }
        if (!connectionIsClosed){
            this.rollback();
        }
    }