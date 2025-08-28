    private void handleDownload(DownloadStream stream, ResourceRequest request,
            ResourceResponse response) throws IOException {

        if (stream.getParameter("Location") != null) {
            response.setProperty(ResourceResponse.HTTP_STATUS_CODE,
                    Integer.toString(HttpServletResponse.SC_MOVED_TEMPORARILY));
            response.setProperty("Location", stream.getParameter("Location"));
            return;
        }

        // Download from given stream
        final InputStream data = stream.getStream();
        if (data != null) {

            // Sets content type
            response.setContentType(stream.getContentType());

            // Sets cache headers
            final long cacheTime = stream.getCacheTime();
            if (cacheTime <= 0) {
                response.setProperty("Cache-Control", "no-cache");
                response.setProperty("Pragma", "no-cache");
                response.setProperty("Expires", "0");
            } else {
                response.setProperty("Cache-Control", "max-age=" + cacheTime
                        / 1000);
                response.setProperty("Expires", "" + System.currentTimeMillis()
                        + cacheTime);
                // Required to apply caching in some Tomcats
                response.setProperty("Pragma", "cache");
            }

            // Copy download stream parameters directly
            // to HTTP headers.
            final Iterator<String> i = stream.getParameterNames();
            if (i != null) {
                while (i.hasNext()) {
                    final String param = i.next();
                    response.setProperty(param, stream.getParameter(param));
                }
            }

            // suggest local filename from DownloadStream if Content-Disposition
            // not explicitly set
            String contentDispositionValue = stream
                    .getParameter("Content-Disposition");
            if (contentDispositionValue == null) {
                contentDispositionValue = "filename=\"" + stream.getFileName()
                        + "\"";
                response.setProperty("Content-Disposition",
                        contentDispositionValue);
            }

            int bufferSize = stream.getBufferSize();
            if (bufferSize <= 0 || bufferSize > MAX_BUFFER_SIZE) {
                bufferSize = DEFAULT_BUFFER_SIZE;
            }
            final byte[] buffer = new byte[bufferSize];
            int bytesRead = 0;

            final OutputStream out = response.getPortletOutputStream();

            while ((bytesRead = data.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
                out.flush();
            }
            out.close();
        }
    }
