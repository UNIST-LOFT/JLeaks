public static HttpResponse toSeleniumResponse(Response response) 
{
    HttpResponse toReturn = new HttpResponse();
    toReturn.setStatus(response.code());
    toReturn.setContent(response.body() == null ? empty() : Contents.memoize(() -> {
        InputStream stream = response.body().byteStream();
        return new InputStream() {

            @Override
            public int read() throws IOException {
                return stream.read();
            }

            @Override
            public void close() throws IOException {
                response.close();
                super.close();
            }
        };
    }));
    response.headers().names().forEach(name -> response.headers(name).forEach(value -> toReturn.addHeader(name, value)));
    // We need to close the okhttp body in order to avoid leaking connections,
    // however if we do this then we can't read the contents any more. We're
    // already memoising the result, so read everything to be safe.
    try {
        ByteStreams.copy(toReturn.getContent().get(), ByteStreams.nullOutputStream());
    } catch (IOException e) {
        throw new UncheckedIOException(e);
    } finally {
        response.close();
    }
    return toReturn;
}