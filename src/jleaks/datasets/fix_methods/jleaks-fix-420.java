public void close() 
{
    logger.info("Shutting down Ethereum instance...");
    worldManager.close();
    ((AbstractApplicationContext) getApplicationContext()).close();
}