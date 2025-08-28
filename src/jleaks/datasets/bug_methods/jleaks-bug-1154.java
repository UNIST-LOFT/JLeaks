    public void close() throws Exception {
        if (!this.getJdbcConnection().isClosed()){
            this.rollback();
        }
    }