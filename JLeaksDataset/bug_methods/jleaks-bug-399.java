    static LDAPConnectionPool createConnectionPool(RealmConfig config, ServerSet serverSet, TimeValue timeout, ESLogger logger) {
        Settings settings = config.settings();
        SimpleBindRequest bindRequest = bindRequest(settings);
        int initialSize = settings.getAsInt("user_search.pool.initial_size", DEFAULT_CONNECTION_POOL_INITIAL_SIZE);
        int size = settings.getAsInt("user_search.pool.size", DEFAULT_CONNECTION_POOL_SIZE);
        try {
            LDAPConnectionPool pool = new LDAPConnectionPool(serverSet, bindRequest, initialSize, size);
            pool.setRetryFailedOperationsDueToInvalidConnections(true);
            if (settings.getAsBoolean("user_search.pool.health_check.enabled", true)) {
                String entryDn = settings.get("user_search.pool.health_check.dn", (bindRequest == null) ? null : bindRequest.getBindDN());
                if (entryDn == null) {
                    pool.close();
                    throw new IllegalArgumentException("[bind_dn] has not been specified so a value must be specified for [user_search" +
                            ".pool.health_check.dn] or [user_search.pool.health_check.enabled] must be set to false");
                }
                long healthCheckInterval = settings.getAsTime("user_search.pool.health_check.interval", DEFAULT_HEALTH_CHECK_INTERVAL)
                        .millis();
                // Checks the status of the LDAP connection at a specified interval in the background. We do not check on
                // on create as the LDAP server may require authentication to get an entry. We do not check on checkout
                // as we always set retry operations and the pool will handle a bad connection without the added latency on every operation
                GetEntryLDAPConnectionPoolHealthCheck healthCheck = new GetEntryLDAPConnectionPoolHealthCheck(entryDn, timeout.millis(),
                        false, false, false, true, false);
                pool.setHealthCheck(healthCheck);
                pool.setHealthCheckIntervalMillis(healthCheckInterval);
            }
            return pool;
        } catch (LDAPException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("unable to create connection pool for realm [{}]", e, config.name());
            } else {
                logger.error("unable to create connection pool for realm [{}]: {}", config.name(), e.getMessage());
            }
        }
        return null;
    }
