  private Map<String, Object> makeCall(String url, String body, HttpCallMethod callMethod) throws RuntimeException {
    try {
      Response response;
      if(body == null) {
        response = client.newCall(getRequest(url, callMethod)).execute();
      }
      else {
        response = client.newCall(getRequest(url, body, callMethod)).execute();
      }
      if(response.code() != HttpURLConnection.HTTP_NOT_FOUND &&
         response.code() != HttpURLConnection.HTTP_SERVER_ERROR &&
         response.code() != HttpURLConnection.HTTP_BAD_REQUEST) {
        return objectMapper.readValue(response.body().string(), HashMap.class);
      } else {
        if(response.body() != null) {
          throw new KubernetesClientException(response.body().string());
        } else {
          response.close();
          throw new KubernetesClientException(response.message());
        }
      }
    } catch(Exception e) {
      throw KubernetesClientException.launderThrowable(e);
    }
  }
