public static ClientBuilder createHttpClientBuilder(
    boolean noSsl,
    boolean trustAllSslCerts,
    Path keystoreFile,
    String keystorePassword,
    Path truststoreFile,
    String truststorePassword) {
    ClientBuilder clientBuilder = ClientBuilder.newBuilder();
    try {
        if (!noSsl) {
            SSLContext sslcontext = SSLContext.getInstance("TLS");
            TrustManager[] trustManagers;
            if (trustAllSslCerts) {
                trustManagers = new TrustManager[] { new X509TrustManager() {

                    @Override
                    public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                } };
                clientBuilder.hostnameVerifier(new TrustAllHostNameVerifier());
            } else if (truststoreFile != null) {
                TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
                KeyStore ts = KeyStore.getInstance("JKS");
                if (truststorePassword == null) {
                    throw new BatfishException("Truststore file supplied but truststore password missing");
                }
                char[] tsPass = truststorePassword.toCharArray();
                try (FileInputStream trustInputStream = new FileInputStream(truststoreFile.toFile())) {
                    ts.load(trustInputStream, tsPass);
                }
                tmf.init(ts);
                trustManagers = tmf.getTrustManagers();
            } else {
                trustManagers = null;
            }
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            KeyStore ks = KeyStore.getInstance("JKS");
            KeyManager[] keyManagers;
            if (keystoreFile != null) {
                if (keystorePassword == null) {
                    throw new BatfishException("Keystore file supplied but keystore password");
                }
                char[] ksPass = keystorePassword.toCharArray();
                try (FileInputStream keystoreStream = new FileInputStream(keystoreFile.toFile())) {
                    ks.load(keystoreStream, ksPass);
                }
                kmf.init(ks, ksPass);
                keyManagers = kmf.getKeyManagers();
            } else {
                keyManagers = null;
            }
            sslcontext.init(keyManagers, trustManagers, new java.security.SecureRandom());
            clientBuilder.sslContext(sslcontext);
        }
    } catch (Exception e) {
        throw new BatfishException("Error creating HTTP client builder", e);
    }
    return clientBuilder;
}