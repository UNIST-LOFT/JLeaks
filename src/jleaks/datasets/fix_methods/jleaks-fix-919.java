default String readString() throws IOException 
{
    StringBuilder result = new StringBuilder();
    String ln;
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(getUrl().openStream()))) {
        while ((ln = reader.readLine()) != null) {
            result.append(ln).append('\n');
        }
    }
    return result.toString();
}