private String createTempContextXml(String docBase, Context ctx) throws IOException 
{
    // NOI18N
    File tmpContextXml = File.createTempFile("context", ".xml");
    tmpContextXml.deleteOnExit();
    if (!docBase.equals(ctx.getAttributeValue("docBase"))) {
        // NOI18N
        // NOI18N
        ctx.setAttributeValue("docBase", docBase);
        try (FileOutputStream fos = new FileOutputStream(tmpContextXml)) {
            ctx.write(fos);
        }
    }
    // http://www.netbeans.org/issues/show_bug.cgi?id=167139
    URL url = tmpContextXml.toURI().toURL();
    // NOI18N
    String ret = URLEncoder.encode(url.toString(), StandardCharsets.UTF_8);
    return ret;
}