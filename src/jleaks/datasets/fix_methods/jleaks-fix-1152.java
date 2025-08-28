public void close() {
            try {
                if (rs != null) {
                    rs.close();

                    // log the query
                    long afterClose = System.currentTimeMillis();
                    logger.debug("total: {} ms, execution: {} ms, reading and parsing: {} ms; executed [{}]", new Object[]{
                            afterClose - start,
                            afterExecQuery-start,
                            afterClose - afterExecQuery,
                            name
                    });

                    rs = null;
                }
            }
            catch (SQLException ex) {
                throw new Sql2oException("Error closing ResultSet.", ex);
            }
            finally {
                if (this.isAutoCloseConnection()){
                    connection.close();
                } else {
                    closeConnectionIfNecessary();
                }
            }
        }