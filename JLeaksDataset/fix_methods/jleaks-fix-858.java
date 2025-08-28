protected void doGet(final HttpServletRequest req, final HttpServletResponse resp){
    resp.setStatus(HttpServletResponse.SC_OK);
    resp.setContentType(TextFormat.CONTENT_TYPE_004);
    Writer writer = resp.getWriter();
    try {
        TextFormat.write004(writer, registry.metricFamilySamples());
        writer.flush();
    } finally {
        writer.close();
    }
}