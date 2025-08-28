
    private List<String> getModifiedLogs() {
        List<String> modifiedLogs = new ArrayList<>();
        try {
            //Opens a URL connection to obtain the entity modified logs
            URL url = new URL(TestProperties.TEAMMATES_URL + "/entityModifiedLogs");

            URLConnection urlConn = url.openConnection();

            BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
            String logMessage;
            while ((logMessage = in.readLine()) != null) {
                modifiedLogs.add(logMessage);
            }
            in.close();
        } catch (IOException e) {
            System.out.println("Error occurred while trying to access modified entity logs: " + e.getMessage());
        }
        return modifiedLogs;
    }
