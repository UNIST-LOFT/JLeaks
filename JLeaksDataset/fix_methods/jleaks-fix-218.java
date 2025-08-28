static <Req, Resp> StreamingRoute<Req, Resp> toStreaming(
    final BlockingStreamingRoute<Req, Resp> original) {
    requireNonNull(original);
    return new StreamingRoute<Req, Resp>() {

        private final AsyncCloseable closeable = toAsyncCloseable(original);

        @Override
        public Publisher<Resp> handle(final GrpcServiceContext ctx, final Publisher<Req> request) {
            return request.firstOrError().map(req -> {
                try {
                    return original.handle(ctx, req);
                } catch (Exception e) {
                    return throwException(e);
                }
            }).toPublisher();
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