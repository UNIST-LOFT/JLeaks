public String get( String urlAsString, String username, String password ){
    HttpGet getMethod = new HttpGet(urlAsString);
    try (CloseableHttpClient httpClient = openHttpClient(username, password)) {
        HttpResponse httpResponse = httpClient.execute(getMethod);
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        StringBuilder bodyBuffer = new StringBuilder();
        if (statusCode != -1) {
            if (statusCode != HttpStatus.SC_UNAUTHORIZED) {
                // the response
                InputStreamReader inputStreamReader = new InputStreamReader(httpResponse.getEntity().getContent());
                int c;
                while ((c = inputStreamReader.read()) != -1) {
                    bodyBuffer.append((char) c);
                }
                inputStreamReader.close();
            } else {
                throw new AuthenticationException();
            }
        }
        // Display response
        return bodyBuffer.toString();
    }
}