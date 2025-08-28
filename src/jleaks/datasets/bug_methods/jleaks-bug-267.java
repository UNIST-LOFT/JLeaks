  public String execute0(String[] args) {
    validateArguments(args);
    if (help) {
      return "";
    }

    String baseUrl = String.format("%s:%d", host, port);
    System.out.println(String.format("Authentication server address is: %s", baseUrl));

    HttpClient client = new DefaultHttpClient();
    HttpResponse response;

    // construct the full URL and verify its well-formedness
    try {
      URI.create(baseUrl);
    } catch (IllegalArgumentException e) {
      System.err.println("Invalid base URL '" + baseUrl + "'. Check the validity of --host or --port arguments.");
      return null;
    }

    HttpGet get = new HttpGet(baseUrl);
    String auth = Base64.encodeBase64String(String.format("%s:%s", username, password).getBytes());
    get.addHeader("Authorization", String.format("Basic %s", auth));

    System.out.println(String.format("Authenticating as: %s", username));
    try {
      response = client.execute(get);
      client.getConnectionManager().shutdown();
    } catch (IOException e) {
      System.err.println("Error sending HTTP request: " + e.getMessage());
      return null;
    }
    if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
      System.out.println("Authentication failed. Please ensure that the username and password provided are correct.");
      return null;
    } else {
      try {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ByteStreams.copy(response.getEntity().getContent(), bos);
        String responseBody = bos.toString("UTF-8");
        bos.close();
        JsonParser parser = new JsonParser();
        JsonObject responseJson = (JsonObject) parser.parse(responseBody);
        String token = responseJson.get(ExternalAuthenticationServer.ResponseFields.ACCESS_TOKEN).getAsString();

        PrintWriter writer = new PrintWriter("access_token", "UTF-8");
        writer.write(token);
        writer.close();
      } catch (Exception e) {
        System.err.println("Could not parse response contents.");
        e.printStackTrace(System.err);
        return null;
      }
      return "OK.";
    }
  }
