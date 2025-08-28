public static String streamToString(InputStream stream) throws IOException 
{
    try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
        StringBuilder buffer = new StringBuilder();
        int b;
        while ((b = bufferedReader.read()) != -1) {
            buffer.append((char) b);
        }
        return buffer.toString();
    }
}