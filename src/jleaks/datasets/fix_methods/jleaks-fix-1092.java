public String getCurrentJobURL(String job){
    String url = null;
    try {
        URL obj = new URL(String.format(JOB_API, jenkinsURL, job));
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.getResponseCode();
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
        try {
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
        } catch (Exception e) {
            LOGGER.debug("Error during FileWriter append. " + e.getMessage(), e.getCause());
        } finally {
            try {
                in.close();
            } catch (Exception e) {
                LOGGER.debug("Error during FileWriter close. " + e.getMessage(), e.getCause());
            }
        }
        XmlPath xmlPath = new XmlPath(response.toString());
        if (xmlPath.getBoolean("freeStyleProject.lastBuild.building")) {
            url = String.format(JOB, jenkinsURL, job, xmlPath.getString("freeStyleProject.lastBuild.number").trim());
        }
    } catch (Exception e) {
        url = "";
        LOGGER.error(e.getMessage());
    }
    return url;
}