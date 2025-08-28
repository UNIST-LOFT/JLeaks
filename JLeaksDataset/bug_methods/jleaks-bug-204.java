    public synchronized void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);

        if (directProxyHandler != null && directProxyHandler.outboundChannel != null) {
            directProxyHandler.outboundChannel.close();
            directProxyHandler = null;
        }

        service.getClientCnxs().remove(this);
        LOG.info("[{}] Connection closed", remoteAddress);

        if (connectionPool != null) {
            try {
                connectionPool.close();
                connectionPool = null;
            } catch (Exception e) {
                LOG.error("Failed to close connection pool {}", e.getMessage(), e);
            }
        }

        state = State.Closed;
    }
