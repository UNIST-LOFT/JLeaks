public Response get() 
{
    String connectorIcon = connector.getIcon();
    if (connectorIcon == null) {
        return Response.status(Response.Status.NOT_FOUND).build();
    }
    if (connectorIcon.startsWith("db:")) {
        String connectorIconId = connectorIcon.substring(3);
        return fromDatabase(connectorIconId);
    } else if (connectorIcon.startsWith("extension:")) {
        String iconFile = connectorIcon.substring(10);
        return fromExtension(iconFile);
    }
    // If the specified icon is a data URL, or a non-URL like value (e.g.
    // font awesome class name), return 404
    if (connectorIcon.startsWith("data:") || !connectorIcon.contains("/")) {
        return Response.status(Response.Status.NOT_FOUND).build();
    }
    final OkHttpClient httpClient = new OkHttpClient();
    try {
        final okhttp3.Response externalResponse = httpClient.newCall(new Request.Builder().get().url(connectorIcon).build()).execute();
        final String contentType = externalResponse.header(CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM);
        final String contentLength = externalResponse.header(CONTENT_LENGTH);
        final StreamingOutput streamingOutput = (out) -> {
            try (Sink iosink = Okio.sink(out);
                BufferedSink sink = Okio.buffer(iosink);
                ResponseBody body = externalResponse.body();
                BufferedSource source = body.source()) {
                sink.writeAll(source);
            }
        };
        final Response.ResponseBuilder actualResponse = Response.ok(streamingOutput, contentType);
        if (!StringUtils.isEmpty(contentLength)) {
            actualResponse.header(CONTENT_LENGTH, contentLength);
        }
        return actualResponse.build();
    } catch (final IOException e) {
        throw new SyndesisServerException(e);
    }
}