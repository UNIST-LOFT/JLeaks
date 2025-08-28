    public Table executeAndFetchTable(){
        ResultSet rs;
        try {
            rs = statement.executeQuery();
        } catch (SQLException e) {
            throw new Sql2oException("Error while executing query", e);
        }
        
        Table table = TableFactory.createTable(rs, this.isCaseSensitive());
        
        return table;
    }