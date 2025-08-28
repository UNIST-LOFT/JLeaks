      public Connection call() throws Exception {
        LOG.info("Creating a connection for entity "
                + context.getEntityAttribute(DataImporter.NAME) + " with URL: "
                + url);
        long start = System.currentTimeMillis();
        Connection c = null;
        try {
          if(url != null){
            c = DriverManager.getConnection(url, initProps);
          } else if(jndiName != null){
            InitialContext ctx =  new InitialContext();
            Object jndival =  ctx.lookup(jndiName);
            if (jndival instanceof javax.sql.DataSource) {
              javax.sql.DataSource dataSource = (javax.sql.DataSource) jndival;
              String user = (String) initProps.get("user");
              String pass = (String) initProps.get("password");
              if(user == null || user.trim().equals("")){
                c = dataSource.getConnection();
              } else {
                c = dataSource.getConnection(user, pass);
              }
            } else {
              throw new DataImportHandlerException(SEVERE,
                      "the jndi name : '"+jndiName +"' is not a valid javax.sql.DataSource");
            }
          }
        } catch (SQLException e) {
          // DriverManager does not allow you to use a driver which is not loaded through
          // the class loader of the class which is trying to make the connection.
          // This is a workaround for cases where the user puts the driver jar in the
          // solr.home/lib or solr.home/core/lib directories.
          Driver d = (Driver) DocBuilder.loadClass(driver, context.getSolrCore()).newInstance();
          c = d.connect(url, initProps);
        }
        if (c != null) {
          if (Boolean.parseBoolean(initProps.getProperty("readOnly"))) {
            c.setReadOnly(true);
            // Add other sane defaults
            c.setAutoCommit(true);
            c.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
            c.setHoldability(ResultSet.CLOSE_CURSORS_AT_COMMIT);
          }
          if (!Boolean.parseBoolean(initProps.getProperty("autoCommit"))) {
            c.setAutoCommit(false);
          }
          String transactionIsolation = initProps.getProperty("transactionIsolation");
          if ("TRANSACTION_READ_UNCOMMITTED".equals(transactionIsolation)) {
            c.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
          } else if ("TRANSACTION_READ_COMMITTED".equals(transactionIsolation)) {
            c.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
          } else if ("TRANSACTION_REPEATABLE_READ".equals(transactionIsolation)) {
            c.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
          } else if ("TRANSACTION_SERIALIZABLE".equals(transactionIsolation)) {
            c.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
          } else if ("TRANSACTION_NONE".equals(transactionIsolation)) {
            c.setTransactionIsolation(Connection.TRANSACTION_NONE);
          }
          String holdability = initProps.getProperty("holdability");
          if ("CLOSE_CURSORS_AT_COMMIT".equals(holdability)) {
            c.setHoldability(ResultSet.CLOSE_CURSORS_AT_COMMIT);
          } else if ("HOLD_CURSORS_OVER_COMMIT".equals(holdability)) {
            c.setHoldability(ResultSet.HOLD_CURSORS_OVER_COMMIT);
          }
        }
        LOG.info("Time taken for getConnection(): "
                + (System.currentTimeMillis() - start));
        return c;
      }