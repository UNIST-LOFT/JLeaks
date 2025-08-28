    public Sql2o executeUpdate(){
        int result;
        try{
            result = statement.executeUpdate();
        }
        catch(Exception ex){
            this.sql2O.rollback();
            throw new RuntimeException(ex);
        }
        finally {
            closeConnectionIfNecessary();
        }

        return this.sql2O;
    }