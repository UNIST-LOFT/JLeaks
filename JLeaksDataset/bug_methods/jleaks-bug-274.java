  private boolean flowletExists(String appId, String flowId, String flowletId, String apiKey)
    throws OperationException {
    String path = String.format("/apps/%s/flows/%s", appId, flowId);
    HttpRequest request = createRequest(HttpMethod.GET, apiKey, path);
    InternalHttpResponse response = sendInternalRequest(request);
    // OK means it exists, NOT_FOUND means it doesn't, and anything else means there was some problem.
    if (response.getStatusCode() == HttpResponseStatus.OK.getCode()) {
      JsonObject flowSpec = new Gson().fromJson(new InputStreamReader(response.getInputStream()), JsonObject.class);
      if (flowSpec != null && flowSpec.has("flowlets")) {
        JsonObject flowlets = flowSpec.getAsJsonObject("flowlets");
        return flowlets.has(flowletId);
      }
    } else if (response.getStatusCode() == HttpResponseStatus.NOT_FOUND.getCode()) {
      return false;
    } else {
      String msg = String.format("got a %d while checking if flowlet %s from flow %s and app %s exists",
                                 response.getStatusCode(), flowletId, flowId, appId);
      throw new OperationException(StatusCode.INTERNAL_ERROR, msg);
    }
    return false;
  }
