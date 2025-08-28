public static SyntaxHighlighter build(Path nanorc, String syntaxName) 
{
    SyntaxHighlighter out = new SyntaxHighlighter();
    InputStream inputStream;
    try {
        if (nanorcUrl.startsWith("classpath:")) {
            inputStream = new Source.ResourceSource(nanorcUrl.substring(10), null).read();
        } else {
            inputStream = new Source.URLSource(new URL(nanorcUrl), null).read();
        }
        NanorcParser parser = new NanorcParser(inputStream, null, null);
        parser.parse();
        out.addRules(parser.getHighlightRules());
    } catch (IOException e) {
        // ignore
    }
    return out;
}