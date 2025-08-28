
    private synchronized void flushQueue() throws IOException {
        if (requestContext == null) {
            return;
        }

        T t;
        while ((t = queue.poll()) != null) {
            requestContext.getWorkers().writeTo(
                    t,
                    t.getClass(),
                    chunkType.getType(),
                    responseContext.getEntityAnnotations(),
                    responseContext.getMediaType(),
                    responseContext.getHeaders(),
                    requestContext.getPropertiesDelegate(),
                    responseContext.getEntityStream(),
                    null,
                    true);
        }

        if (closed) {
            responseContext.getEntityStream().flush();
            responseContext.getEntityStream().close();
            requestContext.getResponseWriter().commit();
        }
    }
