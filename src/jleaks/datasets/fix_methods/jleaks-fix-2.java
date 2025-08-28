private byte[] download(String textUrl) 
{
    URL url = buildURLForConnection(textUrl);
    HttpURLConnection connection = getHttpConnection(url);
    BufferedInputStream bis = null;
    ByteArrayOutputStream baos = null;
    byte[] buffer = null;
    int len;
    if (!urlExists(connection)) {
        return null;
    }
    /**
     * TODO: After minSdkVersion="19" use try-with-resources here.
     */
    try {
        bis = new BufferedInputStream(connection.getInputStream(), 8192);
        baos = new ByteArrayOutputStream();
        buffer = new byte[1024];
        while ((len = bis.read(buffer)) > -1) {
            baos.write(buffer, 0, len);
        }
        baos.flush();
        buffer = baos.toByteArray();
    } catch (Exception e) {
        e(CoverAsyncHelper.class.getSimpleName(), "Failed to download cover :" + e);
    } finally {
        if (bis != null) {
            try {
                bis.close();
            } catch (IOException e) {
                Log.e(CoverAsyncHelper.class.getSimpleName(), "Failed to close the BufferedInputStream.", e);
            }
        }
        if (baos != null) {
            try {
                baos.close();
            } catch (IOException e) {
                Log.e(MPDApplication.TAG, "Failed to close the BufferedArrayOutputStream.", e);
            }
        }
        if (connection != null) {
            connection.disconnect();
        }
    }
    return buffer;
}