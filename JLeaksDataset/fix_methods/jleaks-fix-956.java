private Watch parse(String id, boolean includeStatus, boolean withSecrets, BytesReference source, DateTime now,
                        XContentType xContentType, boolean allowRedactedPasswords) throws IOException {
        if (logger.isTraceEnabled()) {
            logger.trace("parsing watch [{}] ", source.utf8ToString());
        }
        try (InputStream stream = source.streamInput();
             WatcherXContentParser parser = new WatcherXContentParser(xContentType.xContent()
                     // EMPTY is safe here because we never use namedObject
                     .createParser(NamedXContentRegistry.EMPTY, LoggingDeprecationHandler.INSTANCE, stream),
                     new HaltedClock(now), withSecrets ? cryptoService : null, allowRedactedPasswords)) {
            parser.nextToken();
            return parse(id, includeStatus, parser);
        } catch (IOException ioe) {
            throw ioException("could not parse watch [{}]", ioe, id);
        }
    }