public String getCharset(Bucket bucket, String parseCharset) throws DataFilterException, IOException 
{
    logMINOR = Logger.shouldLog(Logger.MINOR, this);
    if (logMINOR)
        Logger.minor(this, "getCharset(): default=" + parseCharset);
    InputStream strm = bucket.getInputStream();
    BufferedInputStream bis = new BufferedInputStream(strm, 4096);
    Writer w = new NullWriter();
    Reader r;
    try {
        r = new BufferedReader(new InputStreamReader(bis, parseCharset), 4096);
    } catch (UnsupportedEncodingException e) {
        strm.close();
        throw e;
    }
    HTMLParseContext pc = new HTMLParseContext(r, w, null, new NullFilterCallback());
    try {
        pc.run(null);
    } catch (IOException e) {
        throw e;
    } catch (Throwable t) {
        // Ignore ALL errors
        if (logMINOR)
            Logger.minor(this, "Caught " + t + " trying to detect MIME type with " + parseCharset);
    }
    try {
        r.close();
    } catch (IOException e) {
        throw e;
    } catch (Throwable t) {
        if (logMINOR)
            Logger.minor(this, "Caught " + t + " closing stream after trying to detect MIME type with " + parseCharset);
    }
    if (logMINOR)
        Logger.minor(this, "Returning charset " + pc.detectedCharset);
    return pc.detectedCharset;
}