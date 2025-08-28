static LDAPConnectionPool createConnectionPool(RealmConfig config, ServerSet serverSet, TimeValue timeout, ESLogger logger) 
{
    Settings settings = config.settings();
    SimpleBindRequest bindRequest = bindRequest(settings);
    final int initialSize = settings.getAsInt("user_search.pool.initial_size", DEFAULT_CONNECTION_POOL_INITIAL_SIZE);
    final int size = settings.getAsInt("user_search.pool.size", DEFAULT_CONNECTION_POOL_SIZE);
    LDAPConnectionPool pool = null;
    boolean success = false;
    try {
        pool = new LDAPConnectionPool(serverSet, bindRequest, initialSize, size);
        pool.setRetryFailedOperationsDueToInvalidConnections(true);
        if (settings.getAsBoolean("user_search.pool.health_check.enabled", true)) {
            String entryDn = settings.get("user_search.pool.health_check.dn", (bindRequest == null) ? null : bindRequest.getBindDN());
            final long healthCheckInterval = settings.getAsTime("user_search.pool.health_check.interval", DEFAULT_HEALTH_CHECK_INTERVAL).millis();
            if (entryDn != null) {
                // Checks the status of the LDAP connection at a specified interval in the background. We do not check on
                // on create as the LDAP server may require authentication to get an entry and a bind request has not been executed
                // yet so we could end up never getting a connection. We do not check on checkout as we always set retry operations
                // and the pool will handle a bad connection without the added latency on every operation
                LDAPConnectionPoolHealthCheck healthCheck = new GetEntryLDAPConnectionPoolHealthCheck(entryDn, timeout.millis(), false, false, false, true, false);
                pool.setHealthCheck(healthCheck);
                pool.setHealthCheckIntervalMillis(healthCheckInterval);
            } else {
                logger.warn("[bind_dn] and [user_search.pool.health_check.dn] have not been specified so no " + "ldap query will be run as a health check");
            }
        }
        success = true;
        return pool;
    } finally {
        if (success == false && pool != null) {
            pool.close();
        }
    }
}