public static void propagate(final String[] hoststubs, final int httpport, final int httpsport, final String peername) 
{
    for (String hoststub : hoststubs) {
        if (hoststub.endsWith("/"))
            hoststub = hoststub.substring(0, hoststub.length() - 1);
        ClientConnection connection = null;
        try {
            connection = new ClientConnection(hoststub + "/api/hello.json?port.http=" + httpport + "&port.https=" + httpsport + "&peername=" + peername);
        } catch (IOException e) {
        } finally {
            if (connection != null)
                connection.close();
        }
    }
}