public Connection call() throws Exception 
{
    LOG.info("Creating a connection for entity " + context.getEntityAttribute(DataImporter.NAME) + " with URL: " + url);
    long start = System.currentTimeMillis();
    Connection c = null;
    if (jndiName != null) {
        c = getFromJndi(initProps, jndiName);
    } else if (url != null) {
        try {
            c = DriverManager.getConnection(url, initProps);
        } catch (SQLException e) {
            // DriverManager does not allow you to use a driver which is not loaded through
            // the class loader of the class which is trying to make the connection.
            // This is a workaround for cases where the user puts the driver jar in the
            // solr.home/lib or solr.home/core/lib directories.
            Driver d = (Driver) DocBuilder.loadClass(driver, context.getSolrCore()).newInstance();
            c = d.connect(url, initProps);
        }
    }
    if (c != null) {
        try {
            initializeConnection(c, initProps);
        } catch (SQLException e) {
            try {
                c.close();
            } catch (SQLException e2) {
                LOG.warn("Exception closing connection during cleanup", e2);
            }
            throw new DataImportHandlerException(SEVERE, "Exception initializing SQL connection", e);
        }
    }
    LOG.info("Time taken for getConnection(): " + (System.currentTimeMillis() - start));
    return c;
}