public static List<String> readFile(Reader simpleReader) throws IOException 
{
    BufferedReader reader = new BufferedReader(simpleReader);
    try {
        List<String> res = new ArrayList();
        String line = null;
        while ((line = reader.readLine()) != null) {
            res.add(line);
        }
        return res;
    } finally {
        reader.close();
    }
}