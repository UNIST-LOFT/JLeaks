public String locate(String name, StatementContext ctx) throws Exception{
    if (looksLikeSql(name)) {
        return name;
    }
    final ClassLoader loader = selectClassLoader();
    InputStream in_stream = loader.getResourceAsStream(name);
    final BufferedReader reader = new BufferedReader(new InputStreamReader(in_stream));
    try {
        if (in_stream == null) {
            in_stream = loader.getResourceAsStream(name + ".sql");
        }
        if (in_stream == null) {
            return name;
        }
        final StringBuffer buffer = new StringBuffer();
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                if (isComment(line)) {
                    // comment
                    continue;
                }
                buffer.append(line).append(" ");
            }
        } catch (IOException e) {
            throw new UnableToCreateStatementException(e.getMessage(), e);
        }
        return buffer.toString();
    } finally {
        try {
            reader.close();
        } catch (IOException e) {
            // nothing we can do here :-(
        }
    }
}