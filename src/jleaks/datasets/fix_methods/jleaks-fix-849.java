public static String readContentAsString(InputStream is, String encoding) 
{
    InputStream is = null;
    try {
        is = new FileInputStream(file);
        StringWriter result = new StringWriter();
        PrintWriter out = new PrintWriter(result);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, encoding));
        String line = null;
        while ((line = reader.readLine()) != null) {
            out.println(line);
        }
        return result.toString();
    } catch (IOException e) {
        throw new UnexpectedException(e);
    } finally {
        if (is != null) {
            try {
                is.close();
            } catch (Exception e) {
                // 
            }
        }
    }
}