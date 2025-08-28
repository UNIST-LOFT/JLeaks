    protected String executeGetRequestWithConnection(String request) {

        URL url = CoverManager.buildURLForConnection(request);
        HttpURLConnection connection = CoverManager.getHttpConnection(url);
        BufferedReader br;
        String result = null;
        String line;

        if (!CoverManager.urlExists(connection)) {
            return null;
        }

        try {
            InputStream inputStream = connection.getInputStream();
            br = new BufferedReader(new InputStreamReader(inputStream));
            line = br.readLine();
            result = line;
            while ((line = br.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            e(CoverAsyncHelper.class.getSimpleName(), "Failed to execute cover get request :" + e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return result;
    }
