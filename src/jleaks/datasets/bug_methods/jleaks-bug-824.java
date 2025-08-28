    protected synchronized void freeUnenlistedResource(ResourceHandle h) {
        if (this.cleanupResource(h)) {
            if (h instanceof AssocWithThreadResourceHandle) {
                if (!((AssocWithThreadResourceHandle) h).isAssociated()) {
                    ds.returnResource(h);
                }
                //update monitoring data
                if (poolLifeCycleListener != null) {
                    poolLifeCycleListener.decrementConnectionUsed(h.getId());
                    poolLifeCycleListener.incrementNumConnFree(false, steadyPoolSize);
                }

                if (maxConnectionUsage_ > 0) {
                    performMaxConnectionUsageOperation(h);
                }
                notifyWaitingThreads();
            }
        }
    }
