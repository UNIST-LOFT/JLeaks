  protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
      throws ServletException, IOException {
    resp.setStatus(HttpServletResponse.SC_OK);
    resp.setContentType(TextFormat.CONTENT_TYPE_004);

    Writer writer = resp.getWriter();
    TextFormat.write004(writer, registry.metricFamilySamples());
    writer.flush();
    writer.close();
  }
