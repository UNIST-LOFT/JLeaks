    public static List<String> readFile(Reader simpleReader) throws IOException {
        BufferedReader reader = new BufferedReader(simpleReader);
        List<String> res = new ArrayList();
        String line = null;
        while ((line = reader.readLine()) != null) {
            res.add(line);
        }
        reader.close();
        return res;
    }
