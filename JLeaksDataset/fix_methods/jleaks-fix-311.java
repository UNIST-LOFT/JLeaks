    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("TunnelClient error, tunnel server url: " + tunnelClient.getTunnelServerUrl(), cause);
        if (!registerPromise.isDone()) {
            registerPromise.setFailure(cause);
        }
        ctx.close();
    }
