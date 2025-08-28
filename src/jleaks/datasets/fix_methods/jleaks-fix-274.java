private boolean flowletExists(String appId, String flowId, String flowletId, String apiKey){
    String path = String.format("/apps/%s/flows/%s", appId, flowId);
    HttpRequest request = createRequest(HttpMethod.GET, apiKey, path);
    InternalHttpResponse response = sendInternalRequest(request);
    // OK means it exists, NOT_FOUND means it doesn't, and anything else means there was some problem.
    boolean exists = false;
    if (response.getStatusCode() == HttpResponseStatus.OK.getCode()) {
        // TODO: add an app-fabric thrift endpoint and a corresponding gateway endpoint for getting a flowlet spec
        // so we dont need to look inside the json returned.
        Reader reader = null;
        try {
            reader = new InputStreamReader(response.getInputStream());
            JsonObject flowSpec = new Gson().fromJson(reader, JsonObject.class);
            if (flowSpec != null && flowSpec.has("flowlets")) {
                JsonObject flowlets = flowSpec.getAsJsonObject("flowlets");
                exists = flowlets.has(flowletId);
            }
        } catch (Exception e) {
            String msg = String.format("Error reading response for the specification for app %s and flow %s.", appId, flowId);
            LOG.error(msg);
            throw new OperationException(StatusCode.INTERNAL_ERROR, msg, e);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                LOG.error("Error closing reader while reading the response for the specification for app {} and flow {}", appId, flowId, e);
            }
        }
    } else if (response.getStatusCode() == HttpResponseStatus.NOT_FOUND.getCode()) {
        exists = false;
    } else {
        String msg = String.format("got a %d while checking if flowlet %s from flow %s and app %s exists", response.getStatusCode(), flowletId, flowId, appId);
        throw new OperationException(StatusCode.INTERNAL_ERROR, msg);
    }
    return exists;
}