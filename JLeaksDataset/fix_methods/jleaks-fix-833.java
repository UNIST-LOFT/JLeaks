public static String encodeBase64ZippedString( String in ) throws IOException 
{
    Charset charset = Charset.forName(Const.XML_ENCODING);
    ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
    try (Base64OutputStream base64OutputStream = new Base64OutputStream(baos);
        GZIPOutputStream gzos = new GZIPOutputStream(base64OutputStream)) {
        gzos.write(in.getBytes(charset));
    }
    return baos.toString();
}