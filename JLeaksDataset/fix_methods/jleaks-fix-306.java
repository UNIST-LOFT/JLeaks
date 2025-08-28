private void partialContentResponse(String range, HttpRequest request, String index, final String digest){
    assert range != null : "Getting partial response but no byte-range is not present.";
    Matcher matcher = CONTENT_RANGE_PATTERN.matcher(range);
    if (!matcher.matches()) {
        LOGGER.warn("Invalid byte-range: {}; returning full content", range);
        fullContentResponse(request, index, digest);
        return;
    }
    BlobShard blobShard = localBlobShard(index, digest);
    final RandomAccessFile raf = blobShard.blobContainer().getRandomAccessFile(digest);
    long start;
    long end;
    try {
        try {
            start = Long.parseLong(matcher.group(1));
            if (start > raf.length()) {
                LOGGER.warn("416 Requested Range not satisfiable");
                simpleResponse(HttpResponseStatus.REQUESTED_RANGE_NOT_SATISFIABLE, null);
                raf.close();
                return;
            }
            end = raf.length() - 1;
            if (!matcher.group(2).equals("")) {
                end = Long.parseLong(matcher.group(2));
            }
        } catch (NumberFormatException ex) {
            LOGGER.error("Couldn't parse Range Header", ex);
            start = 0;
            end = raf.length();
        }
        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, PARTIAL_CONTENT);
        HttpHeaders.setContentLength(response, end - start + 1);
        response.headers().set(CONTENT_RANGE, "bytes " + start + "-" + end + "/" + raf.length());
        setDefaultGetHeaders(response);
        ctx.getChannel().write(response);
        ChannelFuture writeFuture = transferFile(digest, raf, start, end - start + 1);
        if (!HttpHeaders.isKeepAlive(request)) {
            writeFuture.addListener(ChannelFutureListener.CLOSE);
        }
    } catch (Throwable t) {
        /**
         * Make sure RandomAccessFile is closed when exception is raised.
         * In case of success, the ChannelFutureListener in "transferFile" will take care
         * that the resources are released.
         */
        raf.close();
        throw t;
    }
}