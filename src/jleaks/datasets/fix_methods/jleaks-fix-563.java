public static SSLContext createDefaultSSLContext(final String pathToPKCS12File, final String keystorePassword) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException, KeyManagementException, IOException 
{
    String algorithm = Security.getProperty("ssl.KeyManagerFactory.algorithm");
    if (algorithm == null) {
        algorithm = DEFAULT_ALGORITHM;
    }
    if (keyStore.size() == 0) {
        throw new KeyStoreException("Keystore is empty; while this is legal for keystores in general, APNs clients must have at least one key.");
    }
    final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(algorithm);
    trustManagerFactory.init((KeyStore) null);
    final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(algorithm);
    keyManagerFactory.init(keyStore, keyStorePassword);
    final SSLContext sslContext = SSLContext.getInstance(PROTOCOL);
    sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
    return sslContext;
}