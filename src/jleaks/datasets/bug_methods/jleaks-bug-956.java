    private Watch parse(String id, boolean includeStatus, boolean withSecrets, BytesReference source, DateTime now,
                        XContentType xContentType, boolean allowRedactedPasswords) throws IOException {
        if (logger.isTraceEnabled()) {
            logger.trace("parsing watch [{}] ", source.utf8ToString());
        }
        XContentParser parser = null;
        try {
            // EMPTY is safe here because we never use namedObject
            parser = new WatcherXContentParser(xContentType.xContent()
                    .createParser(NamedXContentRegistry.EMPTY, LoggingDeprecationHandler.INSTANCE, source.streamInput()),
                    new HaltedClock(now), withSecrets ? cryptoService : null, allowRedactedPasswords);
            parser.nextToken();
            return parse(id, includeStatus, parser);
        } catch (IOException ioe) {
            throw ioException("could not parse watch [{}]", ioe, id);
        } finally {
            if (parser != null) {
                parser.close();
            }
        }
    }

