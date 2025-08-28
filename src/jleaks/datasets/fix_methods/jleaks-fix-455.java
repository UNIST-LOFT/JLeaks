private String readDefaultMarkdown(String file, String lc) 
{
    String base = file.substring(0, file.lastIndexOf('.'));
    String ext = file.substring(file.lastIndexOf('.'));
    String lc = getLanguageCode();
    String cc = getCountryCode();
    // try to read file_en-us.ext, file_en.ext, file.ext
    List<String> files = new ArrayList<String>();
    if (!StringUtils.isEmpty(lc)) {
        if (!StringUtils.isEmpty(cc)) {
            files.add(base + "_" + lc + "-" + cc + ext);
            files.add(base + "_" + lc + "_" + cc + ext);
        }
        files.add(base + "_" + lc + ext);
    }
    files.add(file);
    for (String name : files) {
        String message;
        InputStreamReader reader = null;
        try {
            ContextRelativeResource res = WicketUtils.getResource(name);
            InputStream is = res.getResourceStream().getInputStream();
            reader = new InputStreamReader(is, Constants.CHARACTER_ENCODING);
            message = MarkdownUtils.transformMarkdown(reader);
            reader.close();
            return message;
        } catch (ResourceStreamNotFoundException t) {
            continue;
        } catch (Throwable t) {
            message = MessageFormat.format(getString("gb.failedToReadMessage"), file);
            error(message, t, false);
            return message;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                }
            }
        }
    }
    return MessageFormat.format(getString("gb.failedToReadMessage"), file);
}