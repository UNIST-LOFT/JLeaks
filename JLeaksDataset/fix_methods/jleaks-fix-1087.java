private void setRootca(KeyStore rootca) 
{
    this.rootca = rootca;
    final StringWriter sw = new StringWriter();
    if (rootca != null) {
        try {
            final Certificate cert = rootca.getCertificate(SslCertificateService.ZAPROXY_JKS_ALIAS);
            try (final PemWriter pw = new PemWriter(sw)) {
                pw.writeObject(new MiscPEMGenerator(cert));
                pw.flush();
            }
        } catch (final Exception e) {
            logger.error("Error while extracting public part from generated Root CA certificate.", e);
        }
    }
    if (logger.isDebugEnabled()) {
        logger.debug("Certificate defined.\n" + sw.toString());
    }
    txt_PubCert.setText(sw.toString());
}