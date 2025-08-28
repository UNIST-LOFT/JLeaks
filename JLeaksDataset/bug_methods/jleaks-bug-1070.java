    public io.undertow.server.session.Session getSession(HttpServerExchange exchange, SessionConfig config) {
        if (config == null) {
            throw UndertowMessages.MESSAGES.couldNotFindSessionCookieConfig();
        }

        Batcher<Batch> batcher = this.manager.getBatcher();
        @SuppressWarnings("resource")
        Batch batch = batcher.createBatch();
        try {
            String id = config.findSessionId(exchange);
            if (id == null) {
                batch.close();
                return null;
            }

            // If requested id contains invalid characters, then session cannot exist and would otherwise cause session lookup to fail
            try {
                Base64.getUrlDecoder().decode(id);
            } catch (IllegalArgumentException e) {
                batch.close();
                return null;
            }

            Session<LocalSessionContext> session = this.manager.findSession(id);
            if (session == null) {
                batch.close();
                return null;
            }
            return new DistributableSession(this, session, config, batcher.suspendBatch());
        } catch (RuntimeException | Error e) {
            batch.discard();
            batch.close();
            throw e;
        }
    }
