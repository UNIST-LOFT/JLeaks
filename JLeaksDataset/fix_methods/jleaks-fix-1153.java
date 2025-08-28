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
            this.connection.rollback();
            throw new Sql2oException("Database error occurred while running executeScalar", e);
        }
        finally{
            closeConnectionIfNecessary();
        }
    }