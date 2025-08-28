        private Collection<X509CRL> loadFromURI(CertificateFactory cf, URI remoteURI) throws GeneralSecurityException {
            try {
                logger.debugf("Loading CRL from %s", remoteURI.toString());

                HttpClient httpClient = session.getProvider(HttpClientProvider.class).getHttpClient();
                HttpGet get = new HttpGet(remoteURI);
                get.setHeader("Pragma", "no-cache");
                get.setHeader("Cache-Control", "no-cache, no-store");
                HttpResponse response = httpClient.execute(get);
                InputStream content = response.getEntity().getContent();
                try {
                    X509CRL crl = loadFromStream(cf, content);
                    return Collections.singleton(crl);
                } finally {
                    content.close();
                }
            }
            catch(IOException ex) {
                logger.errorf(ex.getMessage());
            }
            return Collections.emptyList();

        }
