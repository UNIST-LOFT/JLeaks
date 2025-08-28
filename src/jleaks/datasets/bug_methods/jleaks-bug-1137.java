    public void stop() {
        if (transactionMaintenanceTask != null) {
            logger.debug("Stopping transactionMaintenanceTask...");
            transactionMaintenanceTask.cancel(true);
        }
    }
