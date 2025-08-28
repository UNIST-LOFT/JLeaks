public static final void discardEntityBytes(HttpResponse response) 
{
    HttpEntity entity = response.getEntity();
    if (entity != null) {
        InputStream is = null;
        try {
            is = response.getEntity().getContent();
            while (is.read() != -1) {
                // loop until all bytes read
            }
        } catch (Exception e) {
            Timber.e(e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Timber.d(e);
                }
            }
        }
    }
}