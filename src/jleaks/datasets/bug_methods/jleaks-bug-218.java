static <Req, Resp> StreamingRoute<Req, Resp> toStreaming(
            final BlockingStreamingRoute<Req, Resp> original) {
        requireNonNull(original);
        return new StreamingRoute<Req, Resp>() {
            private final AsyncCloseable closeable = toAsyncCloseable(original);
            @Override
            public Publisher<Resp> handle(final GrpcServiceContext ctx, final Publisher<Req> request) {
                return new Publisher<Resp>() {
                    @Override
                    protected void handleSubscribe(final Subscriber<? super Resp> subscriber) {
                        ConnectablePayloadWriter<Resp> connectablePayloadWriter = new ConnectablePayloadWriter<>();
                        Publisher<Resp> pub = connectablePayloadWriter.connect();
                        Subscriber<? super Resp> concurrentTerminalSubscriber =
                                new ConcurrentTerminalSubscriber<>(subscriber, false);
                        toSource(pub).subscribe(concurrentTerminalSubscriber);
                        try {
                            original.handle(ctx, request.toIterable(), new GrpcPayloadWriter<Resp>() {
                                @Override
                                public void write(final Resp resp) throws IOException {
                                    connectablePayloadWriter.write(resp);
                                }

                                @Override
                                public void close() throws IOException {
                                    connectablePayloadWriter.close();
                                }

                                @Override
                                public void flush() throws IOException {
                                    connectablePayloadWriter.flush();
                                }
                            });
                        } catch (Throwable t) {
                            concurrentTerminalSubscriber.onError(t);
                        }
                    }
                };
            }
            @Override
            public Completable closeAsync() {
                return closeable.closeAsync();
            }
            @Override
            public Completable closeAsyncGracefully() {
                return closeable.closeAsyncGracefully();
            }
        };
    }