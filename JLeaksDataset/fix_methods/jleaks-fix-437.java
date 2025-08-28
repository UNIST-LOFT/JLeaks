public static boolean push(String[] hoststubs, Timeline timeline) 
{
    // transmit the timeline
    assert timeline.size() != 0;
    if (timeline.size() == 0)
        return true;
    try {
        String data = new ObjectMapper().writer().writeValueAsString(timeline.toMap(false));
        assert data != null;
        boolean transmittedToAtLeastOnePeer = false;
        for (String hoststub : hoststubs) {
            if (hoststub.endsWith("/"))
                hoststub = hoststub.substring(0, hoststub.length() - 1);
            Map<String, byte[]> post = new HashMap<String, byte[]>();
            // optionally implement a gzipped form here
            post.put("data", UTF8.getBytes(data));
            ClientConnection connection = null;
            try {
                connection = new ClientConnection(hoststub + "/api/push.json", post);
                transmittedToAtLeastOnePeer = true;
            } catch (IOException e) {
                // e.printStackTrace();
            } finally {
                if (connection != null)
                    connection.close();
            }
        }
        return transmittedToAtLeastOnePeer;
    } catch (JsonProcessingException e) {
        e.printStackTrace();
        return false;
    }
}