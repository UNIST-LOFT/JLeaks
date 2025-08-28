    private InputStream getCommandResponse(String command, InputStream is) throws IOException {
        int responsecode = HttpURLConnection.HTTP_MOVED_PERM;
        while (responsecode == HttpURLConnection.HTTP_MOVED_PERM) {
            URL result = new URL(pathToServlet); 
            String body = buildCommandBody(command);
            HttpURLConnection uc = (HttpURLConnection) result.openConnection();
            uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
            uc.setInstanceFollowRedirects(false);
            uc.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(uc.getOutputStream());
            try {
                wr.write(body);
                wr.flush();
            } finally {
                wr.close();
            }
            responsecode = uc.getResponseCode();
            if (responsecode == HttpURLConnection.HTTP_MOVED_PERM) {
                pathToServlet = uc.getRequestProperty("Location");
            } else if (responsecode != HttpURLConnection.HTTP_OK) {
                throw new SeleniumException(uc.getResponseMessage());
            } else {
                is = uc.getInputStream();
            }
        }
        return is;
    }
