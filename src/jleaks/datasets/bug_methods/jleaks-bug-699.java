    public JsonFactory write(JSONObject json) throws IOException {
        String line = json.toString(); // new ObjectMapper().writer().writeValueAsString(map);
        JsonFactory jf = null;
        byte[] b = line.getBytes(StandardCharsets.UTF_8);
        long seekpos = this.json_log.appendLine(b);
        jf = this.json_log.getJsonFactory(seekpos, b.length);
        return jf;
    }
