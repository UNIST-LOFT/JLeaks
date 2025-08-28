    private static int pingHealthEndpoint(URI remote) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) remote.toURL().openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        return conn.getResponseCode();
    }
