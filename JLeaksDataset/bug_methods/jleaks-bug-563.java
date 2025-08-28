	public static SSLContext createDefaultSSLContext(final String pathToPKCS12File, final String keystorePassword) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException, KeyManagementException, IOException {
		final FileInputStream keystoreInputStream = new FileInputStream(pathToPKCS12File);
		return createDefaultSSLContext(keystoreInputStream, keystorePassword);
	}
