public CompletableFuture<byte[]> execute(String uri, byte[] payload) 
{
    return HttpResponse.from(req.aggregateWithPooledObjects(ctx.eventLoop(), ctx.alloc()).thenCompose(msg -> {
        try (HttpData content = msg.content()) {
            final ByteBuf buf = content.byteBuf();
            final HttpData framed;
            try (ArmeriaMessageFramer framer = new ArmeriaMessageFramer(ctx.alloc(), Integer.MAX_VALUE, isGrpcWebText)) {
                framed = framer.writePayload(buf);
            }
            try {
                return unwrap().execute(ctx, HttpRequest.of(req.headers(), framed)).aggregateWithPooledObjects(ctx.eventLoop(), ctx.alloc());
            } catch (Exception e) {
                throw new ArmeriaStatusException(StatusCodes.INTERNAL, "Error executing request.");
            }
        }
    }).thenCompose(msg -> {
        if (msg.status() != HttpStatus.OK || msg.content().isEmpty()) {
            // Status can either be in the headers or trailers depending on error.
            if (msg.headers().get(GrpcHeaderNames.GRPC_STATUS) != null) {
                GrpcWebTrailers.set(ctx, msg.headers());
            } else {
                GrpcWebTrailers.set(ctx, msg.trailers());
            }
            // Nothing to deframe.
            return CompletableFuture.completedFuture(msg.toHttpResponse());
        }
        final CompletableFuture<HttpResponse> responseFuture = new CompletableFuture<>();
        final ArmeriaMessageDeframer deframer = new ArmeriaMessageDeframer(Integer.MAX_VALUE);
        msg.toHttpResponse().decode(deframer, ctx.alloc(), byteBufConverter(ctx.alloc(), isGrpcWebText)).subscribe(new DeframedMessageSubscriber(ctx, msg, serializationFormat, responseFuture), ctx.eventLoop(), SubscriptionOption.WITH_POOLED_OBJECTS);
        return responseFuture;
    }), ctx.eventLoop());
}