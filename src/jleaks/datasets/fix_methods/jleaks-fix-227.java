    public String getVeryGamesIp(Player player) {
        String realIP = player.getAddress().getAddress().getHostAddress();
        String sUrl = "http://monitor-1.verygames.net/api/?action=ipclean-real-ip&out=raw&ip=%IP%&port=%PORT%";
        sUrl = sUrl.replace("%IP%", player.getAddress().getAddress().getHostAddress())
                   .replace("%PORT%", "" + player.getAddress().getPort());
        try {
            URL url = new URL(sUrl);
            URLConnection urlCon = url.openConnection();
            try (BufferedReader in = new BufferedReader(new InputStreamReader(urlCon.getInputStream()))) {
                String inputLine = in.readLine();
                if (!StringUtils.isEmpty(inputLine) && !inputLine.equalsIgnoreCase("error")
                        && !inputLine.contains("error")) {
                    realIP = inputLine;
                }
            } catch (IOException e) {
                ConsoleLogger.showError("Could not read from Very Games API - " + StringUtils.formatException(e));
            }
        } catch (IOException e) {
            ConsoleLogger.showError("Could not fetch Very Games API with URL '" + sUrl + "' - "
                + StringUtils.formatException(e));
        }
        return realIP;
    }
