    public static boolean push(String[] hoststubs, Timeline timeline) {
        // transmit the timeline
        assert timeline.size() != 0;
        if (timeline.size() == 0) return true;
        
        try {
            String data = new ObjectMapper().writer().writeValueAsString(timeline.toMap(false));
            assert data != null;
            boolean transmittedToAtLeastOnePeer = false;
            for (String hoststub: hoststubs) {
                if (hoststub.endsWith("/")) hoststub = hoststub.substring(0, hoststub.length() - 1);
                Map<String, byte[]> post = new HashMap<String, byte[]>();
                post.put("data", UTF8.getBytes(data)); // optionally implement a gzipped form here
                try {
                    ClientConnection connection = new ClientConnection(hoststub + "/api/push.json", post);
                    connection.close();
                    transmittedToAtLeastOnePeer = true;
                } catch (IOException e) {
                    //e.printStackTrace();
                }
            }
            return transmittedToAtLeastOnePeer;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return false;
        }
    }
