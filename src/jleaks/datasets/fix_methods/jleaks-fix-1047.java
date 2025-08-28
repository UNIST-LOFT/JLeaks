
    public static String readAll(Reader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader buffered = new BufferedReader(reader)) {
            int c;
            while ((c = buffered.read()) != -1)
                sb.appendCodePoint(c);
        }
        return sb.toString();
    }