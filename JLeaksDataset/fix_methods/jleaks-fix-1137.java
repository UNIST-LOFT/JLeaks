public void stop() {
        if(taskExecutor != null) {
            logger.debug("Stopping Http Site-to-Site Transaction Maintenance task...");
            taskExecutor.shutdown();
        }
        if (transactionMaintenanceTask != null) {
            logger.debug("Stopping transactionMaintenanceTask...");
            transactionMaintenanceTask.cancel(true);
        }
    }