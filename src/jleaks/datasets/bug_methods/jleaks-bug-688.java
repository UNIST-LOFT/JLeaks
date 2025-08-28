    public CompletableFuture<byte[]> execute(String uri, byte[] payload) {
        final HttpRequest request = HttpRequest.of(
                RequestHeaders.builder(HttpMethod.POST, uri).contentType(serializationFormat.mediaType())
                              .add(HttpHeaderNames.TE, HttpHeaderValues.TRAILERS.toString()).build(),
                HttpData.wrap(payload));
        return webClient.execute(request).aggregate()
                        .thenApply(msg -> {
                            if (!HttpStatus.OK.equals(msg.status())) {
                                throw new ArmeriaStatusException(
                                        StatusCodes.INTERNAL,
                                        "Non-successful HTTP response code: " + msg.status());
                            }

                            // Status can either be in the headers or trailers depending on error
                            String grpcStatus = msg.headers().get(GrpcHeaderNames.GRPC_STATUS);
                            if (grpcStatus != null) {
                                checkGrpcStatus(grpcStatus, msg.headers());
                            } else {
                                grpcStatus = msg.trailers().get(GrpcHeaderNames.GRPC_STATUS);
                                checkGrpcStatus(grpcStatus, msg.trailers());
                            }

                            return msg.content().array();
                        });
    }
