    protected synchronized void freeUnenlistedResource(ResourceHandle h) {
        if (this.cleanupResource(h)) {
            if (h instanceof AssocWithThreadResourceHandle) {
                //Only when resource handle usage count is more than maxConnUsage
                if (maxConnectionUsage_ > 0 &&
                        h.getUsageCount() >= maxConnectionUsage_) {
                    performMaxConnectionUsageOperation(h);
                } else {

                    if (!((AssocWithThreadResourceHandle) h).isAssociated()) {
                        ds.returnResource(h);
                    }
                    //update monitoring data
                    if (poolLifeCycleListener != null) {
                        poolLifeCycleListener.decrementConnectionUsed(h.getId());
                        poolLifeCycleListener.incrementNumConnFree(false, steadyPoolSize);
                    }
                }
                //for both the cases of free.add and maxConUsageOperation, a free resource is added.
                // Hence notify waiting threads
                notifyWaitingThreads();
            }
        }
    }
