  private String httpGet(HttpUrl url) throws IOException {
    Request request = new Request.Builder()
        .url(url)
        .get()
        .build();

    Response response = client.newCall(request).execute();
    throwOnCommonError(response);
    return response.body().string();
  }
