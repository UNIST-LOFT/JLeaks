    public void close()
    {
        if (!closed) {
            statementBuilder.close(getConnection());
            try {
                connection.close();
            }
            catch (SQLException e) {
                throw new UnableToCloseResourceException("Unable to close Connection", e);
            }
            finally {
                log.logReleaseHandle(this);
                closed = true;
            }
        }
    }
