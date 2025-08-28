    public void close() {
        if (log.isDebugEnabled()) {
            log.debug("Closing repository and connection to Elasticsearch ...");
        }

        // bail out if closed before
        if (client == null) {
            return;
        }

        if (!hadWriteErrors) {
            flush();
        }
        else {
            if (log.isDebugEnabled()) {
                log.debug("Dirty close; ignoring last existing write batch...");
            }
        }

        if (requiresRefreshAfterBulk && executedBulkWrite) {
            // refresh batch
            client.refresh(resourceW);

            if (log.isDebugEnabled()) {
                log.debug(String.format("Refreshing index [%s]", resourceW));
            }
        }

        client.close();
        stats.aggregate(client.stats());
        client = null;
    }
