  public void select(@ConsoleParameter(name = "query-text", description = "The query to execute") String iQueryText) {
    checkForDatabase();

    if (iQueryText == null)
      return;

    iQueryText = iQueryText.trim();

    if (iQueryText.length() == 0 || iQueryText.equalsIgnoreCase("select"))
      return;

    iQueryText = "select " + iQueryText;

    final int queryLimit;
    final int displayLimit;
    if (iQueryText.toLowerCase(Locale.ENGLISH).contains(" limit ")) {
      queryLimit = -1;
      displayLimit = -1;
    } else {
      // USE LIMIT + 1 TO DISCOVER IF MORE ITEMS ARE PRESENT
      displayLimit = Integer.parseInt(properties.get("limit"));
      if (displayLimit > 0) {
        queryLimit = displayLimit + 1;
      } else {
        queryLimit = -1;
      }
    }

    final long start = System.currentTimeMillis();
    List<OIdentifiable> result = new ArrayList<>();
    OResultSet rs = currentDatabase.query(iQueryText);
    int count = 0;
    while (rs.hasNext() && (queryLimit < 0 || count < queryLimit)) {
      OResult item = rs.next();
      if (item.isBlob()) {
        result.add(item.getBlob().get());
      } else {
        result.add(item.toElement());
      }
    }
    rs.close();
    setResultset(result);

    float elapsedSeconds = getElapsedSecs(start);

    dumpResultSet(displayLimit);

    long tot = displayLimit > -1 ? Math.min(currentResultSet.size(), displayLimit) : currentResultSet.size();
    message("\n\n" + tot + " item(s) found. Query executed in " + elapsedSeconds + " sec(s).");
  }
