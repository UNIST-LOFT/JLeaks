protected void writeStaticResourceResponse(HttpServletRequest request,
HttpServletResponse response, URL resourceUrl) throws IOException {
    // Write the resource to the client.
    URLConnection connection = resourceUrl.openConnection();
    try {
        int length = connection.getContentLength();
        if (length >= 0) {
            response.setContentLength(length);
        }
    } catch (Throwable e) {
        // This can be ignored, content length header is not required.
        // Need to close the input stream because of
        // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4257700 to
        // prevent it from hanging, but that is done below.
    }
    InputStream is = connection.getInputStream();
    try {
        final OutputStream os = response.getOutputStream();
        final byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        int bytes;
        while ((bytes = is.read(buffer)) >= 0) {
            os.write(buffer, 0, bytes);
        }
    } finally {
        if (is != null) {
            is.close();
        }
    }
}