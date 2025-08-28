private InputStream transformForWindows(InputStream src) throws IOException 
{
    BufferedReader r = new BufferedReader(new InputStreamReader(src));
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try (PrintStream p = new PrintStream(out)) {
        String line;
        while ((line = r.readLine()) != null) {
            if (!line.startsWith("#") && Functions.isWindows())
                line = line.replace("/", "\\\\");
            p.println(line);
        }
    }
    return new ByteArrayInputStream(out.toByteArray());
}