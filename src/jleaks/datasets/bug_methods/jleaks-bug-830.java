  public String get( String urlAsString, String username, String password )
    throws IOException, AuthenticationException {

    HttpClient httpClient;
    HttpGet getMethod = new HttpGet( urlAsString );
    if ( !Utils.isEmpty( username ) ) {
      HttpClientManager.HttpClientBuilderFacade clientBuilder = HttpClientManager.getInstance().createBuilder();
      clientBuilder.setCredentials( username, password );
      httpClient = clientBuilder.build();
    } else {
      httpClient = HttpClientManager.getInstance().createDefaultClient();
    }
    HttpResponse httpResponse = httpClient.execute( getMethod );
    int statusCode = httpResponse.getStatusLine().getStatusCode();
    StringBuilder bodyBuffer = new StringBuilder();

    if ( statusCode != -1 ) {
      if ( statusCode != HttpStatus.SC_UNAUTHORIZED ) {
        // the response
        InputStreamReader inputStreamReader = new InputStreamReader( httpResponse.getEntity().getContent() );

        int c;
        while ( ( c = inputStreamReader.read() ) != -1 ) {
          bodyBuffer.append( (char) c );
        }
        inputStreamReader.close();

      } else {
        throw new AuthenticationException();
      }
    }

    // Display response
    return bodyBuffer.toString();
  }
