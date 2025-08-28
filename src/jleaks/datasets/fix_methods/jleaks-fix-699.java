public JsonFactory write(JSONObject json) throws IOException 
{
    // new ObjectMapper().writer().writeValueAsString(map);
    String line = json.toString();
    JsonFactory jf = null;
    StringBuilder sb = new StringBuilder();
    sb.append('{').append('\"').append(OPERATION_KEY_STRING).append('\"').append(':').append('\"').append(opkey).append('\"').append(',');
    sb.append(line.substring(1));
    byte[] b = sb.toString().getBytes(StandardCharsets.UTF_8);
    try {
        long seekpos = this.json_log.appendLine(b);
        jf = this.json_log.getJsonFactory(seekpos, b.length);
    } catch (IOException e) {
        if (e.getMessage().indexOf("Stream Closed") < 0)
            throw e;
        open();
        long seekpos = this.json_log.appendLine(b);
        jf = this.json_log.getJsonFactory(seekpos, b.length);
        this.json_log.close();
    }
    return jf;
}