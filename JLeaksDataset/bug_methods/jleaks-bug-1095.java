  public BrownCluster(InputStream in) throws IOException {

    BufferedReader breader =
        new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
    String line;
    while ((line = breader.readLine()) != null) {
      String[] lineArray = tabPattern.split(line);
      if (lineArray.length == 3) {
        int freq = Integer.parseInt(lineArray[2]);
        if (freq > 5 ) {
          tokenToClusterMap.put(lineArray[1], lineArray[0]);
        }
      }
      else if (lineArray.length == 2) {
        tokenToClusterMap.put(lineArray[0], lineArray[1]);
      }
    }
  }