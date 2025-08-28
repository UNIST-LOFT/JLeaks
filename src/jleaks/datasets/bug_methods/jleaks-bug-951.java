  public static String slurpURL(URL u, String encoding) throws IOException {
    String lineSeparator = System.lineSeparator();
    URLConnection uc = u.openConnection();
    uc.setReadTimeout(30000);
    InputStream is;
    try {
      is = uc.getInputStream();
    } catch (SocketTimeoutException e) {
      logger.error("Socket time out; returning empty string.");
      logger.err(throwableToStackTrace(e));
      return "";
    }
    BufferedReader br = new BufferedReader(new InputStreamReader(is, encoding));
    StringBuilder buff = new StringBuilder(SLURP_BUFFER_SIZE); // make biggish
    for (String temp; (temp = br.readLine()) != null; ) {
      buff.append(temp);
      buff.append(lineSeparator);
    }
    br.close();
    return buff.toString();
  }
