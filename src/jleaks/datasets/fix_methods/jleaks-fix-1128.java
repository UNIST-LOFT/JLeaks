public SSLContextFactory(final NiFiProperties properties) throws NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException, KeyStoreException, UnrecoverableKeyException {
        keystore = properties.getProperty(NiFiProperties.SECURITY_KEYSTORE);
        keystorePass = getPass(properties.getProperty(NiFiProperties.SECURITY_KEYSTORE_PASSWD));
        keystoreType = properties.getProperty(NiFiProperties.SECURITY_KEYSTORE_TYPE);
        truststore = properties.getProperty(NiFiProperties.SECURITY_TRUSTSTORE);
        truststorePass = getPass(properties.getProperty(NiFiProperties.SECURITY_TRUSTSTORE_PASSWD));
        truststoreType = properties.getProperty(NiFiProperties.SECURITY_TRUSTSTORE_TYPE);

        // prepare the keystore
        final KeyStore keyStore = KeyStore.getInstance(keystoreType);
        final FileInputStream keyStoreStream = new FileInputStream(keystore);
        try {
            keyStore.load(keyStoreStream, keystorePass);
        } finally {
            FileUtils.closeQuietly(keyStoreStream);
        }
        final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, keystorePass);

        // prepare the truststore
        final KeyStore trustStore = KeyStore.getInstance(truststoreType);
        final FileInputStream trustStoreStream = new FileInputStream(truststore);
        try {
            trustStore.load(trustStoreStream, truststorePass);
        } finally {
            FileUtils.closeQuietly(trustStoreStream);
        }
        final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);

        keyManagers = keyManagerFactory.getKeyManagers();
        trustManagers = trustManagerFactory.getTrustManagers();
    }