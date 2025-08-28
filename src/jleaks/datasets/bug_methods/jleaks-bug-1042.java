    protected void writeStaticResourceResponse(HttpServletRequest request,
            HttpServletResponse response, URL resourceUrl) throws IOException {
        // Write the resource to the client.
        final OutputStream os = response.getOutputStream();
        final byte buffer[] = new byte[DEFAULT_BUFFER_SIZE];
        int bytes;
        InputStream is = resourceUrl.openStream();
        while ((bytes = is.read(buffer)) >= 0) {
            os.write(buffer, 0, bytes);
        }
        is.close();
    }
