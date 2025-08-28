protected void decode(ChannelHandlerContext ctx, ByteBuf buffer) throws InterruptedException {
    while (!queue.isEmpty()) {
        RedisCommand<?, ?, ?> command = queue.peek();
        if (debugEnabled) {
            logger.debug("{} Queue contains: {} commands", logPrefix(), queue.size());
        }

        try {
            if (!decode(ctx, buffer, command)) {
                return;
            }
        } catch (Exception e) {

            ctx.close();
            throw e;
        }

        queue.poll();
        try {
            command.complete();
        } catch (Exception e) {
            logger.warn("{} Unexpected exception during request: {}", logPrefix, e.toString(), e);
        }
        if (buffer.refCnt() != 0) {
            buffer.discardReadBytes();
        }
    }
}