    private String callWebService(String key) {
        String url = webServiceUrl.replace("{key}", key);
        HTTPClient client = getHttpClient();

        client.setConnectTimeout(connectTimeout);
        client.setReadTimeout(readTimeout);
        try {
            LOGGER.log(Level.FINE, "Issuing request to authkey webservice: " + url);
            HTTPResponse response = client.get(new URL(url));
            BufferedReader reader = null;
            InputStream responseStream = response.getResponseStream();
            StringBuilder result = new StringBuilder();
            try {
                reader = new BufferedReader(new InputStreamReader(responseStream));
                String line = null;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                LOGGER.log(
                        Level.FINE,
                        "Response received from authkey webservice: " + result.toString());
                return result.toString();
            } finally {
                reader.close();
            }
        } catch (MalformedURLException e) {
            LOGGER.log(
                    Level.SEVERE,
                    "Error in WebServiceAuthenticationKeyMapper, web service url is invalid: "
                            + url,
                    e);
        } catch (IOException e) {
            LOGGER.log(
                    Level.SEVERE,
                    "Error in WebServiceAuthenticationKeyMapper, error in web service communication",
                    e);
        }
        return null;
    }
