    public String getVeryGamesIp(Player player) {
        String realIP = player.getAddress().getAddress().getHostAddress();
        String sUrl = "http://monitor-1.verygames.net/api/?action=ipclean-real-ip&out=raw&ip=%IP%&port=%PORT%";
        sUrl = sUrl.replace("%IP%", player.getAddress().getAddress().getHostAddress()).replace("%PORT%", "" + player.getAddress().getPort());
        try {
            URL url = new URL(sUrl);
            URLConnection urlCon = url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(urlCon.getInputStream()));
            String inputLine = in.readLine();
            if (inputLine != null && !inputLine.isEmpty() && !inputLine.equalsIgnoreCase("error") && !inputLine.contains("error")) {
                realIP = inputLine;
            }
        } catch (Exception ignored) {
        }
        return realIP;
    }
