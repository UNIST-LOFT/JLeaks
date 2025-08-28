public static ECDSASignature decodeFromDER(byte[] bytes) 
{
    ASN1InputStream decoder = null;
    try {
        decoder = new ASN1InputStream(bytes);
        DLSequence seq = (DLSequence) decoder.readObject();
        if (seq == null)
            throw new RuntimeException("Reached past end of ASN.1 stream.");
        ASN1Integer r, s;
        try {
            r = (ASN1Integer) seq.getObjectAt(0);
            s = (ASN1Integer) seq.getObjectAt(1);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(e);
        }
        // OpenSSL deviates from the DER spec by interpreting these values as unsigned, though they should not be
        // Thus, we always use the positive versions. See: http://r6.ca/blog/20111119T211504Z.html
        return new ECDSASignature(r.getPositiveValue(), s.getPositiveValue());
    } catch (IOException e) {
        throw new RuntimeException(e);
    } finally {
        if (decoder != null)
            try {
                decoder.close();
            } catch (IOException x) {
            }
    }
}