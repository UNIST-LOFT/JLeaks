    public Object executeScalar(){
        try {
            ResultSet rs = this.statement.executeQuery();
            if (rs.next()){
                return rs.getObject(1);
            }
            else{
                return null;
            }

        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
        finally{
            closeConnectionIfNecessary();
        }
    }