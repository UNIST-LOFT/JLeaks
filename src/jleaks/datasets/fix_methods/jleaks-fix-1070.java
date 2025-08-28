public io.undertow.server.session.Session getSession(HttpServerExchange exchange, SessionConfig config) 
{
    try (Batch batch = this.manager.getBatcher().createBatch()) {
        try {
            ImmutableSession session = this.manager.viewSession(sessionId);
            return (session != null) ? new DistributableImmutableSession(this, session) : null;
        } catch (RuntimeException | Error e) {
            batch.discard();
            throw e;
        }
    }
}