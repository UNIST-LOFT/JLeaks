private static boolean horribleEvilHack(Bucket data) throws IOException 
{
    InputStream is = null;
    try {
        int sz = (int) Math.min(data.size(), 512);
        if (sz == 0)
            return false;
        is = data.getInputStream();
        byte[] buf = new byte[sz];
        // FIXME Fortunately firefox doesn't detect RSS in UTF16 etc ... yet
        is.read(buf);
        /**
         * Look for any of the following strings:
         * <rss
         * &lt;feed
         * &lt;rdf:RDF
         *
         * If they start at the beginning of the file, or are preceded by one or more &lt;! or &lt;? tags,
         * then firefox will read it as RSS. In which case we must force it to be downloaded to disk.
         */
        if (checkForString(buf, "<rss"))
            return true;
        if (checkForString(buf, "<feed"))
            return true;
        if (checkForString(buf, "<rdf:RDF"))
            return true;
    } finally {
        is.close();
    }
    return false;
}