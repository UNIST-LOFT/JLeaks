private String sendPost(String urlParams) 
{
    try {
        HttpURLConnection con = (HttpURLConnection) rpcUrl.openConnection();
        // add reuqest header
        con.setRequestMethod("POST");
        // Send post request
        con.setDoOutput(true);
        try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
            wr.writeBytes(urlParams);
            wr.flush();
        }
        int responseCode = con.getResponseCode();
        if (responseCode != 200) {
            throw new RuntimeException("HTTP Response: " + responseCode);
        }
        final StringBuffer response;
        try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
            String inputLine;
            response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
        }
        return response.toString();
    } catch (IOException e) {
        throw new RuntimeException("Error sending POST to " + rpcUrl, e);
    }
}