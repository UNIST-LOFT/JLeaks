public JSONObject loadJSONObject(String filename) 
{
    // can't pass of createReader() to the constructor b/c of resource leak
    BufferedReader reader = createReader(file);
    JSONObject outgoing = new JSONObject(reader);
    try {
        reader.close();
    } catch (IOException e) {
        // not sure what would cause this
        e.printStackTrace();
    }
    return outgoing;
}