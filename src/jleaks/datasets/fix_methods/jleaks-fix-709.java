private static KeyManager[] readPKCS12Certificate(String certPath,
String keyPassword) throws NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException, UnrecoverableKeyException, KeyStoreException, SQLServerException {
    KeyStore keystore = KeyStore.getInstance(PKCS12_ALG);
    try (FileInputStream certStream = new FileInputStream(certPath)) {
        keystore.load(certStream, keyPassword.toCharArray());
    } catch (FileNotFoundException e) {
        throw new SQLServerException(SQLServerException.getErrString("R_clientCertError"), null, 0, null);
    }
    KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(SUN_X_509);
    keyManagerFactory.init(keystore, keyPassword.toCharArray());
    return keyManagerFactory.getKeyManagers();
}