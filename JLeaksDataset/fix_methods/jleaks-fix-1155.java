    public Table executeAndFetchTable(){
        ResultSet rs;
        try {
            rs = statement.executeQuery();
            return TableFactory.createTable(rs, this.isCaseSensitive());
        } catch (SQLException e) {
            throw new Sql2oException("Error while executing query", e);
        } finally {
            closeConnectionIfNecessary();
        }
    }