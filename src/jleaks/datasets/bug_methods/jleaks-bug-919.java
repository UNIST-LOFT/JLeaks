    default String readString() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(getUrl().openStream()));
        StringBuilder result = new StringBuilder();
        String ln;
        while ((ln = reader.readLine()) != null) {
            result.append(ln).append('\n');
        }
        reader.close();
        return result.toString();
    }
