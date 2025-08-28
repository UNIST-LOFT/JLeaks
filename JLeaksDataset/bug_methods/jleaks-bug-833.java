  public static String encodeBase64ZippedString( String in ) throws IOException {
    Charset charset = Charset.forName( Const.XML_ENCODING );
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    GZIPOutputStream gzos = new GZIPOutputStream( baos );
    gzos.write( in.getBytes( charset ) );
    gzos.close();

    return new String( Base64.encodeBase64( baos.toByteArray() ) );
  }
