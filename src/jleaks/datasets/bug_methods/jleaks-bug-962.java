    public Response get() {
        String connectorIcon = connector.getIcon();
        if (connectorIcon == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (connectorIcon.startsWith("db:")) {
            String connectorIconId = connectorIcon.substring(3);
            Icon icon = getDataManager().fetch(Icon.class, connectorIconId);
            if (icon == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            //grab icon file from the (Sql)FileStore
            final StreamingOutput streamingOutput = (out) -> {
                try (BufferedSink sink = Okio.buffer(Okio.sink(out));
                    Source source = Okio.source(iconDao.read(connectorIconId))) {
                    sink.writeAll(source);
                }
            };
            return Response.ok(streamingOutput, icon.getMediaType()).build();
        } else if (connectorIcon.startsWith("extension:")) {
            String iconFile = connectorIcon.substring(10);
            Optional<InputStream> extensionIcon = connector.getDependencies().stream()
                .filter(Dependency::isExtension)
                .map(Dependency::getId)
                .findFirst()
                .flatMap(extensionId -> extensionDataManager.getExtensionIcon(extensionId, iconFile));

            if (extensionIcon.isPresent()) {
                final StreamingOutput streamingOutput = (out) -> {
                    try (BufferedSink sink = Okio.buffer(Okio.sink(out)); InputStream iconStream = extensionIcon.get()) {
                        sink.writeAll(Okio.source(iconStream));
                    }
                };
                return Response.ok(streamingOutput, extensionDataManager.getExtensionIconMediaType(iconFile)).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
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
                final BufferedSink sink = Okio.buffer(Okio.sink(out));
                sink.writeAll(externalResponse.body().source());
                sink.close();
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
