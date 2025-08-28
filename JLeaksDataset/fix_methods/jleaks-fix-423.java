private Map<String, Object> makeCall(String url, String body, HttpCallMethod callMethod) throws RuntimeException 
{
    Request request = (body == null) ? getRequest(url, callMethod) : getRequest(url, body, callMethod);
    try (Response response = client.newCall(request).execute()) {
        if (response.isSuccessful()) {
            return objectMapper.readValue(response.body().string(), HashMap.class);
        } else {
            String message = String.format("Error while performing the call to %s. Response code: %s", url, response.code());
            Status status = createStatus(response);
            throw new KubernetesClientException(message, response.code(), status);
        }
    } catch (Exception e) {
        throw KubernetesClientException.launderThrowable(e);
    }
}