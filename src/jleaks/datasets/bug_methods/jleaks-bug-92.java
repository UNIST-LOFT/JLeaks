    public static String streamToString(InputStream stream) throws IOException {
        InputStreamReader isr = new InputStreamReader(stream, StandardCharsets.UTF_8);
        BufferedReader bufferedReader = new BufferedReader(isr);

        int b;
        StringBuilder buffer = new StringBuilder();
        while ((b = bufferedReader.read()) != -1) {
            buffer.append((char) b);
        }

        bufferedReader.close();
        isr.close();
        return buffer.toString();
    }
