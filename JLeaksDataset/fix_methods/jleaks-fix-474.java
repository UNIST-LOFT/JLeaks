public boolean isFileOutOfThisFormat(String filepath) 
{
    if (filepath.matches("(?i)^.+\\.txt$")) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(new File(filepath)));
            return ZimWikiHighlighter.Patterns.ZIMHEADER_CONTENT_TYPE_ONLY.pattern.matcher(reader.readLine()).find();
        } catch (Exception ignored) {
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (Exception ignored) {
            }
        }
    }
    return false;
}