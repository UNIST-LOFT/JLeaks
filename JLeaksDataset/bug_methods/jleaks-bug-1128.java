    public SSLContextFactory(final NiFiProperties properties) throws NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException, KeyStoreException, UnrecoverableKeyException {
        keystore = properties.getProperty(NiFiProperties.SECURITY_KEYSTORE);
        keystorePass = getPass(properties.getProperty(NiFiProperties.SECURITY_KEYSTORE_PASSWD));
        keystoreType = properties.getProperty(NiFiProperties.SECURITY_KEYSTORE_TYPE);

        truststore = properties.getProperty(NiFiProperties.SECURITY_TRUSTSTORE);
        truststorePass = getPass(properties.getProperty(NiFiProperties.SECURITY_TRUSTSTORE_PASSWD));
        truststoreType = properties.getProperty(NiFiProperties.SECURITY_TRUSTSTORE_TYPE);

        // prepare the keystore
        final KeyStore keyStore = KeyStore.getInstance(keystoreType);
        keyStore.load(new FileInputStream(keystore), keystorePass);
        final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, keystorePass);

        // prepare the truststore
        final KeyStore trustStore = KeyStore.getInstance(truststoreType);
        trustStore.load(new FileInputStream(truststore), truststorePass);
        final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);

        keyManagers = keyManagerFactory.getKeyManagers();
        trustManagers = trustManagerFactory.getTrustManagers();
    }
