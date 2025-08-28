    protected void decode(ChannelHandlerContext ctx, ByteBuf buffer) throws InterruptedException {

        while (!queue.isEmpty()) {

            RedisCommand<?, ?, ?> command = queue.peek();
            if (debugEnabled) {
                logger.debug("{} Queue contains: {} commands", logPrefix(), queue.size());
            }

            if (latencyMetricsEnabled && command instanceof WithLatency) {

                WithLatency withLatency = (WithLatency) command;
                if (withLatency.getFirstResponse() == -1) {
                    withLatency.firstResponse(nanoTime());
                }

                if (!decode(ctx, buffer, command)) {
                    return;
                }

                recordLatency(withLatency, command.getType());

            } else {

                if (!decode(ctx, buffer, command)) {
                    return;
                }

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
