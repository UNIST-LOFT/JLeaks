  public void syncCloseAllProcessor() {
    logger.info("Start closing all storage group processor");
    for (StorageGroupProcessor processor : processorMap.values()) {
      processor.waitForAllCurrentTsFileProcessorsClosed();
    }
  }
