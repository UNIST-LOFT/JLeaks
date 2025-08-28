private void frameAndServe(
    ServiceRequestContext ctx,
    HttpHeaders grpcHeaders,
    AggregatedHttpMessage clientRequest,
    DefaultHttpResponse res) {
    final DefaultHttpRequest grpcRequest = new DefaultHttpRequest(grpcHeaders);
    try (ArmeriaMessageFramer framer = new ArmeriaMessageFramer(ctx.alloc(), ArmeriaMessageFramer.NO_MAX_OUTBOUND_MESSAGE_SIZE)) {
        HttpData content = clientRequest.content();
        ByteBuf message = ctx.alloc().buffer(content.length());
        final HttpData frame;
        boolean success = false;
        try {
            message.writeBytes(content.array(), content.offset(), content.length());
            frame = framer.writePayload(message);
            success = true;
        } finally {
            if (!success) {
                message.release();
            }
        }
        grpcRequest.write(frame);
        grpcRequest.close();
    }
    final HttpResponse grpcResponse;
    try {
        grpcResponse = delegate().serve(ctx, grpcRequest);
    } catch (Exception e) {
        res.close(e);
        return;
    }
    grpcResponse.aggregate().whenCompleteAsync((framedResponse, t) -> {
        if (t != null) {
            res.close(t);
        } else {
            deframeAndRespond(ctx, framedResponse, res);
        }
    }, ctx.eventLoop());
}