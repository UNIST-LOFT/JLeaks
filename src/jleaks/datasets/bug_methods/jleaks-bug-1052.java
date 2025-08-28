    public <T> T tryAllConfigServers(CreateRequest requestFactory, Class<T> wantedReturnType) {
        Exception lastException = null;
        for (int loopRetry = 0; loopRetry < MAX_LOOPS; loopRetry++) {
            for (String configServer : configServerHosts) {
                final HttpResponse response;
                try {
                    response = client.execute(requestFactory.createRequest(configServer));
                } catch (Exception e) {
                    lastException = e;
                    NODE_ADMIN_LOGGER.info("Exception while talking to " + configServer + " (will try all config servers):" + e.getMessage());
                    continue;
                }
                if (response.getStatusLine().getStatusCode() == Response.Status.NOT_FOUND.getStatusCode()) {
                    throw new NotFoundException("Not found returned from " + configServer);
                }
                if (response.getStatusLine().getStatusCode() != Response.Status.OK.getStatusCode()) {
                    String entity = read(response.getEntity());
                    NODE_ADMIN_LOGGER.info("Non-200 HTTP response code received:\n" + entity);
                    throw new RuntimeException("Did not get response code 200, but " + response.getStatusLine().getStatusCode() +
                            entity);
                }
                try {
                    return mapper.readValue(response.getEntity().getContent(), wantedReturnType);
                } catch (IOException e) {
                    throw new RuntimeException("Response didn't contain nodes element, failed parsing?", e);
                }
            }
        }
        throw new RuntimeException("Failed executing request, last exception: ", lastException);
    }
