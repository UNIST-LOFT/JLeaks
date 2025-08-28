    private void runContextFilter(final ChannelHandlerContext ctx) {
        Single<Boolean> filterResult;
        try {
            filterResult = contextFilter.filter(context);
        } catch (Throwable t) {
            ctx.close();
            LOGGER.warn("Exception from context filter {} for context {}.", contextFilter, context, t);
            return;
        }
        filterResult.subscribe(new Single.Subscriber<Boolean>() {

            @Override
            public void onSubscribe(final Cancellable cancellable) {
                assert sequentialCancellable != null;

                sequentialCancellable.setNextCancellable(cancellable);
            }

            @Override
            public void onSuccess(@Nullable final Boolean result) {
                if (result != null && result) {
                    // handleSuccess makes multiple calls that will queue things on the event loop already, so we
                    // offload the whole method to the event loop. This also ensures handleSuccess is only run from
                    // the event loop, which means state doesn't need to be handled concurrently.
                    final EventLoop eventLoop = ctx.channel().eventLoop();
                    if (eventLoop.inEventLoop()) {
                        handleSuccess(ctx);
                    } else {
                        eventLoop.execute(() -> handleSuccess(ctx));
                    }
                } else {
                    // Getting the remote-address may involve volatile reads and potentially a syscall, so guard it.
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Rejected connection from {}", context.getRemoteAddress());
                    }
                    ctx.close();
                }
            }

            @Override
            public void onError(final Throwable t) {
                LOGGER.warn("Error from context filter {} for context {}.", contextFilter, context, t);
                ctx.close();
            }
        });
    }
