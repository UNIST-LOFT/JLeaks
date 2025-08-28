  static Template parsedTemplateForResource(String resourceName) {
    InputStream in = AutoValueTemplateVars.class.getResourceAsStream(resourceName);
    if (in == null) {
      throw new IllegalArgumentException("Could not find resource: " + resourceName);
    }
    try {
      Reader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
      return Template.parseFrom(reader);
    } catch (UnsupportedEncodingException e) {
      throw new AssertionError(e);
    } catch (IOException e) {
      throw new AssertionError(e);
    }
  }
