    public static final void discardEntityBytes(HttpResponse response) {
        // may be a server that does not handle
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            try {
                // have to read the stream in order to reuse the connection
                InputStream is = response.getEntity().getContent();
                if (is.available() > 0) {
                    // read to end of stream...
                    final long count = 1024L;
                    while (is.skip(count) == count) {
                        // skipping to the end of the http entity
                    }
                }
                is.close();
            } catch (IOException e) {
                Timber.e(e, "Unable read the stream");
            } catch (Exception e) {
                Timber.e(e);
            }
        }
    }
