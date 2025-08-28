    public static void propagate(final String[] hoststubs, final int httpport, final int httpsport, final String peername) {
        for (String hoststub: hoststubs) {
            if (hoststub.endsWith("/")) hoststub = hoststub.substring(0, hoststub.length() - 1);
            try {
                ClientConnection connection = new ClientConnection(hoststub + "/api/hello.json?port.http=" + httpport + "&port.https=" + httpsport + "&peername=" + peername);
                connection.close();
            } catch (IOException e) {
            }
        }
    }
