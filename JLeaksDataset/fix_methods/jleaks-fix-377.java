public boolean hasNext() 
{
    boolean ret = false;
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
        ret = !buffer.isEmpty();
        return ret;
    } catch (SQLException e) {
        throw cquery.createPersistenceException(e);
    } finally {
        if (!ret) {
            close();
        }
    }
}