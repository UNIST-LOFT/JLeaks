CloseableHttpResponse requestStatusHtml() throws IOException 
{
    try {
        HttpGet request = new HttpGet("https://localhost:" + port + HEALTH_CHECK_PATH);
        return client().execute(request);
    } catch (SSLException e) {
        log.log(Level.SEVERE, "SSL connection failed. Closing existing client, a new client will be created on next request", e);
        close();
        throw e;
    }
}