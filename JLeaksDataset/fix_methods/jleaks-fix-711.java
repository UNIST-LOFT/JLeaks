private byte[] getAttestationCertificates() throws IOException 
{
    byte[] certData = null;
    X509CertificateEntry cacheEntry = certificateCache.get(attestationUrl);
    if (null != cacheEntry && !cacheEntry.expired()) {
        certData = cacheEntry.getCertificates();
    } else if (null != cacheEntry && cacheEntry.expired()) {
        certificateCache.remove(attestationUrl);
    }
    if (null == certData) {
        java.net.URL url = new java.net.URL(attestationUrl + "/attestationservice.svc/v2.0/signingCertificates/");
        java.net.URLConnection con = url.openConnection();
        byte[] buff = new byte[con.getInputStream().available()];
        con.getInputStream().read(buff, 0, buff.length);
        String s = new String(buff);
        // omit the square brackets that come with the JSON
        String[] bytesString = s.substring(1, s.length() - 1).split(",");
        certData = new byte[bytesString.length];
        for (int i = 0; i < certData.length; i++) {
            certData[i] = (byte) (Integer.parseInt(bytesString[i]));
        }
        certificateCache.put(attestationUrl, new X509CertificateEntry(certData));
    }
    return certData;
}