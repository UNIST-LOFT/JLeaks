private Collection<X509CRL> loadFromURI(CertificateFactory cf, URI remoteURI) throws GeneralSecurityException 
{
    try {
        logger.debugf("Loading CRL from %s", remoteURI.toString());
        CloseableHttpClient httpClient = session.getProvider(HttpClientProvider.class).getHttpClient();
        HttpGet get = new HttpGet(remoteURI);
        get.setHeader("Pragma", "no-cache");
        get.setHeader("Cache-Control", "no-cache, no-store");
        try (CloseableHttpResponse response = httpClient.execute(get)) {
            try {
                InputStream content = response.getEntity().getContent();
                X509CRL crl = loadFromStream(cf, content);
                return Collections.singleton(crl);
            } finally {
                EntityUtils.consumeQuietly(response.getEntity());
            }
        }
    } catch (IOException ex) {
        logger.errorf(ex.getMessage());
    }
    return Collections.emptyList();
}