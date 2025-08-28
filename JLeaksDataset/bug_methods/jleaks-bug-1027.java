    public void writeHelperFile(String hostportpath, File helperFile) throws IOException {
        if (!helperFile.exists()) {
            OutputStream file = new FileOutputStream(helperFile, false); // Append
            PrintWriter pw = new PrintWriter(file);
            String vap = (String)survprops.get("CLDR_VAP");
            pw.write("<h3>Survey Tool admin interface link</h3>");
            pw.write("To configure the SurveyTool, use ");
            String url0 = hostportpath + "cldr-setup.jsp" + "?vap=" + vap;
            pw.write("<b>SurveyTool Setup:</b>  <a href='" + url0 + "'>" + url0 + "</a><hr>");
            String url = hostportpath + ("AdminPanel.jsp") + "?vap=" + vap;
            pw.write("<b>Admin Panel:</b>  <a href='" + url + "'>" + url + "</a>");
            pw.write("<hr>if you change the admin password ( CLDR_VAP in config.properties ), please: 1. delete this admin.html file 2. restart the server 3. navigate back to the main SurveyTool page.<p>");
            pw.close();
            file.close();
        }
    }
