  public boolean hasNext() {
    try {
      if (buffer.isEmpty() && moreToLoad) {
        // load buffer
        request.flushPersistenceContextOnIterate();

        int i = -1;
        while (moreToLoad && ++i < bufferSize) {
          if (cquery.hasNext()) {
            buffer.add((T) cquery.next());
          } else {
            moreToLoad = false;
          }
        }
        request.executeSecondaryQueries(true);
      }
      return !buffer.isEmpty();

    } catch (SQLException e) {
      throw cquery.createPersistenceException(e);
    }
  }
