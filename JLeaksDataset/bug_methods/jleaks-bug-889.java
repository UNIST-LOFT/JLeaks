  public static HttpResponse toSeleniumResponse(Response response) {
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

    response.headers().names().forEach(
        name -> response.headers(name).forEach(value -> toReturn.addHeader(name, value)));

    return toReturn;
  }
