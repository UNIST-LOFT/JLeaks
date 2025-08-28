
    protected String getCommandResponseAsString(String command) throws IOException {
        String responseString = null;
        int responsecode = HttpURLConnection.HTTP_MOVED_PERM;
        HttpURLConnection uc = null;
        Writer wr = null;
        Reader rdr = null;
        while (responsecode == HttpURLConnection.HTTP_MOVED_PERM) {
            URL result = new URL(pathToServlet); 
            String body = buildCommandBody(command);
            try {
                uc = getHttpUrlConnection(result);
                uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
                uc.setInstanceFollowRedirects(false);
                uc.setDoOutput(true);
                wr = getOutputStreamWriter(uc);;
                wr.write(body);
                wr.flush();
                responsecode = getResponseCode(uc);
                if (responsecode == HttpURLConnection.HTTP_MOVED_PERM) {
                    pathToServlet = uc.getRequestProperty("Location");
                } else if (responsecode != HttpURLConnection.HTTP_OK) {
                    throw new SeleniumException(uc.getResponseMessage());
                } else {
                    rdr = getInputStreamReader(uc);
                    responseString = stringContentsOfInputStream(rdr);
                }
            } finally {
              closeResources(uc, wr, rdr);
            }
        }
        return responseString;
    }
    