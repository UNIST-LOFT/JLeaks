public void close() 
{
    logger.debug("Closing client");
    if (connection != null) {
        connection.close();
    }
}