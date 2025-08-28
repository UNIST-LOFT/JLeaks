public void doConsoleText(StaplerRequest req, StaplerResponse rsp) throws IOException 
{
    rsp.setContentType("text/plain;charset=UTF-8");
    // Prevent jelly from flushing stream so Content-Length header can be added afterwards
    FlushProofOutputStream out = new FlushProofOutputStream(rsp.getCompressedOutputStream(req));
    try {
        getLogText().writeLogTo(0, out);
    } catch (IOException e) {
        // see comment in writeLogTo() method
        InputStream input = getLogInputStream();
        try {
            IOUtils.copy(input, out);
        } finally {
            IOUtils.closeQuietly(input);
        }
    } finally {
        try {
            out.close();
        } catch (IOException ioe) {
            // swallow exception
        }
    }
}