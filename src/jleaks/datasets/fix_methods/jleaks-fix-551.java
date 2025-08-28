private synchronized void flushQueue() throws IOException
{
    if (requestContext == null) {
        return;
    }
    Exception ex = null;
    try {
        T t;
        while ((t = queue.poll()) != null) {
            requestContext.getWorkers().writeTo(t, t.getClass(), getType(), responseContext.getEntityAnnotations(), responseContext.getMediaType(), responseContext.getHeaders(), requestContext.getPropertiesDelegate(), responseContext.getEntityStream(), null, true);
        }
        // flush the stream for each chunk
        responseContext.commitStream();
    } catch (Exception e) {
        closed = true;
        ex = e;
    } finally {
        if (closed) {
            try {
                responseContext.getEntityStream().close();
            } catch (Exception e) {
                ex = ex == null ? e : ex;
            }
            try {
                requestContext.getResponseWriter().commit();
            } catch (Exception e) {
                ex = ex == null ? e : ex;
            }
            if (ex instanceof IOException) {
                throw (IOException) ex;
            } else if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            }
        }
    }
}