    public Sql2o executeUpdate(){
        int result;
        try{
            result = statement.executeUpdate();
            if (this.sql2O.getConnection().getAutoCommit()){
                this.sql2O.getConnection().close();
                statement.close();
            }
        }
        catch(Exception ex){
            this.sql2O.rollback();
            throw new RuntimeException(ex);
        }

        return this.sql2O;
    }