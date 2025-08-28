public void handle(HttpExchange t) throws IOException 
{
    String query = t.getRequestURI().getRawQuery();
    String contextPath = t.getHttpContext().getPath();
    ByteArrayOutputStream response = this.response.get();
    response.reset();
    OutputStreamWriter osw = new OutputStreamWriter(response, Charset.forName("UTF-8"));
    if ("/-/healthy".equals(contextPath)) {
        osw.write(HEALTHY_RESPONSE);
    } else {
        TextFormat.write004(osw, registry.filteredMetricFamilySamples(parseQuery(query)));
    }
    osw.close();
    t.getResponseHeaders().set("Content-Type", TextFormat.CONTENT_TYPE_004);
    if (shouldUseCompression(t)) {
        t.getResponseHeaders().set("Content-Encoding", "gzip");
        t.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
        final GZIPOutputStream os = new GZIPOutputStream(t.getResponseBody());
        try {
            response.writeTo(os);
        } finally {
            os.close();
        }
    } else {
        t.getResponseHeaders().set("Content-Length", String.valueOf(response.size()));
        t.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.size());
        response.writeTo(t.getResponseBody());
    }
    t.close();
}