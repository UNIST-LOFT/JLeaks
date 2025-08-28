public boolean isValid(int timeout) throws SQLException 
{
    loggerExternal.entering(getClassNameLogging(), "isValid", timeout);
    // Throw an exception if the timeout is invalid
    if (timeout < 0) {
        MessageFormat form = new MessageFormat(SQLServerException.getErrString("R_invalidQueryTimeOutValue"));
        Object[] msgArgs = { timeout };
        SQLServerException.makeFromDriverError(this, this, form.format(msgArgs), null, true);
    }
    // Return false if the connection is closed
    if (isSessionUnAvailable())
        return false;
    boolean isValid = true;
    try (SQLServerStatement stmt = new SQLServerStatement(this, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, SQLServerStatementColumnEncryptionSetting.UseConnectionSetting)) {
        // If asked, limit the time to wait for the query to complete.
        if (0 != timeout)
            stmt.setQueryTimeout(timeout);
        /*
             * Try to execute the query. If this succeeds, then the connection is valid. If it fails (throws an
             * exception), then the connection is not valid. If a timeout was provided, execution throws an
             * "query timed out" exception if the query fails to execute in that time.
             */
        stmt.executeQueryInternal("SELECT 1");
    } catch (SQLException e) {
        isValid = false;
        /*
             * Do not propagate SQLExceptions from query execution or statement closure. The connection is considered to
             * be invalid if the statement fails to close, even though query execution succeeded.
             */
        connectionlogger.fine(toString() + " Exception checking connection validity: " + e.getMessage());
    }
    loggerExternal.exiting(getClassNameLogging(), "isValid", isValid);
    return isValid;
}