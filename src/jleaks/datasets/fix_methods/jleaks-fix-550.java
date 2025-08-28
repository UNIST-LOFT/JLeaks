public <T> T readEntity(Class<T> rawType, Type type, Annotation[] annotations, PropertiesDelegate propertiesDelegate) 
{
    final boolean buffered = entityContent.isBuffered();
    if (buffered) {
        entityContent.reset();
    }
    entityContent.ensureNotClosed();
    // TODO: revise if we need to re-introduce the check for performance reasons or once non-blocking I/O is supported.
    // The code has been commended out because in case of streaming input (e.g. SSE) the call might block until a first
    // byte is available, which would make e.g. the SSE EventSource construction or EventSource.open() method to block
    // until a first event is received, which is undesirable.
    // 
    // if (entityContent.isEmpty()) {
    // return null;
    // }
    if (workers == null) {
        return null;
    }
    MediaType mediaType = getMediaType();
    mediaType = mediaType == null ? MediaType.APPLICATION_OCTET_STREAM_TYPE : mediaType;
    boolean shouldClose = !buffered;
    try {
        T t = (T) workers.readFrom(rawType, type, annotations, mediaType, headers, propertiesDelegate, entityContent.getWrappedStream(), entityContent.hasContent() ? readerInterceptors.get() : Collections.<ReaderInterceptor>emptyList(), translateNce);
        shouldClose = shouldClose && !(t instanceof Closeable) && !(t instanceof Source);
        return t;
    } catch (IOException ex) {
        throw new ProcessingException(LocalizationMessages.ERROR_READING_ENTITY_FROM_INPUT_STREAM(), ex);
    } finally {
        if (shouldClose) {
            entityContent.close();
        }
    }
}