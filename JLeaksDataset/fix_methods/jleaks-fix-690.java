public void onNext(DeframedMessage unframed) 
{
    if (UnaryGrpcSerializationFormats.isGrpcWeb(serializationFormat) && message.isTrailer()) {
        final ByteBuf buf;
        try {
            buf = InternalGrpcWebUtil.messageBuf(message, ctx.alloc());
        } catch (Throwable t) {
            onError(t);
            return;
        }
        try {
            trailers = InternalGrpcWebUtil.parseGrpcWebTrailers(buf);
            if (trailers == null) {
                // Malformed trailers.
                onError(new ArmeriaStatusException(StatusCodes.INTERNAL, serializationFormat.uriText() + " trailers malformed: " + buf.toString(StandardCharsets.UTF_8)));
            }
        } finally {
            buf.release();
        }
        processedMessages++;
        return;
    }
    if (processedMessages > 0) {
        onError(new ArmeriaStatusException(StatusCodes.INTERNAL, "received more than one data message; " + "UnaryGrpcClient does not support streaming."));
        return;
    }
    final ByteBuf buf = message.buf();
    // Compression not supported.
    assert buf != null;
    content = HttpData.wrap(buf);
    processedMessages++;
}