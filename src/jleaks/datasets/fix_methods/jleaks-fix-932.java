public void initConnection() throws JMSException 
{
    if (getTargetConnectionFactory() == null) {
        throw new IllegalStateException("'targetConnectionFactory' is required for lazily initializing a Connection");
    }
    synchronized (this.connectionMonitor) {
        if (this.connection != null) {
            closeConnection(this.connection);
        }
        // Create new (method local) connection, which is later assigned to instance connection
        // - prevention to hold instance connection without exception listener, in case when
        // some subsequent methods (after creation of connection) throws JMSException
        Connection con = doCreateConnection();
        try {
            prepareConnection(con);
            this.connection = con;
        } catch (JMSException ex) {
            // Attempt to close new (not used) connection to release possible resources
            try {
                con.close();
            } catch (Throwable th) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Could not close newly obtained JMS Connection that failed to prepare", th);
                }
            }
            throw ex;
        }
        if (this.startedCount > 0) {
            this.connection.start();
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Established shared JMS Connection: " + this.connection);
        }
    }
}