private void queryDatastore(String tenant, String datastoreName, String query, HttpServletResponse response){
    response.setContentType("application/xhtml+xml");
    datastoreName = datastoreName.replaceAll("\\+", " ");
    final AnalyzerBeansConfiguration configuration = _tenantContextFactory.getContext(tenant).getConfiguration();
    final Datastore ds = configuration.getDatastoreCatalog().getDatastore(datastoreName);
    if (ds == null) {
        response.sendError(HttpServletResponse.SC_NOT_FOUND, "No such datastore: " + datastoreName);
        return;
    }
    String username = getUsername();
    if (StringUtils.isNullOrEmpty(query)) {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No query defined");
        return;
    }
    try (final DatastoreConnection con = ds.openConnection()) {
        final DataContext dataContext = con.getDataContext();
        try (final DataSet dataSet = dataContext.executeQuery(query)) {
            logger.info("Serving query result of datastore {} to user: {}. Query: {}", new Object[] { datastoreName, username, query });
            final Writer writer = response.getWriter();
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            writer.write("\n<table xmlns=\"http://www.w3.org/1999/xhtml\">");
            writer.write("\n<thead>\n<tr>");
            final SelectItem[] selectItems = dataSet.getSelectItems();
            for (SelectItem selectItem : selectItems) {
                final String label = selectItem.getSuperQueryAlias(false);
                writer.write("<th>");
                writer.write(StringEscapeUtils.escapeXml(label));
                writer.write("</th>");
            }
            writer.write("</tr>\n</thead>");
            writer.flush();
            writer.write("\n<tbody>");
            int rowNumber = 1;
            while (dataSet.next()) {
                writer.write("\n<tr>");
                Row row = dataSet.getRow();
                for (int i = 0; i < selectItems.length; i++) {
                    Object value = row.getValue(i);
                    if (value == null) {
                        writer.write("<td />");
                    } else {
                        writer.write("<td>");
                        writer.write(StringEscapeUtils.escapeXml(ConvertToStringTransformer.transformValue(value)));
                        writer.write("</td>");
                    }
                }
                writer.write("</tr>");
                if (rowNumber % 20 == 0) {
                    writer.flush();
                }
                rowNumber++;
            }
            writer.write("\n</tbody>");
            writer.write("\n</table>");
        }
    } catch (QueryParserException e) {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Query parsing failed: " + e.getMessage());
    }
}