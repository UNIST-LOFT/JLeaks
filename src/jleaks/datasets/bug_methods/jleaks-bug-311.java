    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (!registerPromise.isDone()) {
            registerPromise.setFailure(cause);
        }
        ctx.fireExceptionCaught(cause);
    }
