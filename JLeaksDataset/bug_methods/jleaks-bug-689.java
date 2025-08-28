    private void frameAndServe(
            ServiceRequestContext ctx,
            HttpHeaders grpcHeaders,
            AggregatedHttpMessage clientRequest,
            DefaultHttpResponse res) {
        final ArmeriaMessageFramer framer = new ArmeriaMessageFramer(
                ctx.alloc(), ArmeriaMessageFramer.NO_MAX_OUTBOUND_MESSAGE_SIZE);

        HttpData content = clientRequest.content();
        ByteBuf message = ctx.alloc().buffer(content.length());
        message.writeBytes(content.array(), content.offset(), content.length());

        HttpData frame = framer.writePayload(message);
        DefaultHttpRequest grpcRequest = new DefaultHttpRequest(grpcHeaders);
        grpcRequest.write(frame);
        grpcRequest.close();

        final HttpResponse grpcResponse;
        try {
            grpcResponse = delegate().serve(ctx, grpcRequest);
        } catch (Exception e) {
            res.close(e);
            return;
        }

        grpcResponse.aggregate().whenCompleteAsync(
                (framedResponse, t) -> {
                    if (t != null) {
                        res.close(t);
                    } else {
                        deframeAndRespond(ctx, framedResponse, res);
                    }
                },
                ctx.eventLoop());
    }
