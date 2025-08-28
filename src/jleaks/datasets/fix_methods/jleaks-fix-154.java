public void syncCloseAllProcessor() 
{
    logger.info("Start closing all storage group processor");
    for (StorageGroupProcessor processor : processorMap.values()) {
        processor.waitForAllCurrentTsFileProcessorsClosed();
        // TODO do we need to wait for all merging tasks to be finished here?
        processor.closeAllResources();
    }
}