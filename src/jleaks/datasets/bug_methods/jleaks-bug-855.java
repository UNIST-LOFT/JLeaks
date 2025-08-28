  public JSONObject loadJSONObject(String filename) {
    return new JSONObject(createReader(filename));
  }
