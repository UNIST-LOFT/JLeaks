public File download(DownloadJob job, URL src) throws IOException 
{
    CountingInputStream in = null;
    OutputStream out = null;
    try {
        URLConnection con = connect(job, src);
        int total = con.getContentLength();
        in = new CountingInputStream(con.getInputStream());
        byte[] buf = new byte[8192];
        int len;
        File dst = job.getDestination();
        File tmp = new File(dst.getPath() + ".tmp");
        out = new FileOutputStream(tmp);
        LOGGER.info("Downloading " + job.getName());
        try {
            while ((len = in.read(buf)) >= 0) {
                out.write(buf, 0, len);
                job.status = job.new Installing(total == -1 ? -1 : in.getCount() * 100 / total);
            }
        } catch (IOException e) {
            throw new IOException2("Failed to load " + src + " to " + tmp, e);
        }
        if (total != -1 && total != tmp.length()) {
            // don't know exactly how this happens, but report like
            // http://www.ashlux.com/wordpress/2009/08/14/hudson-and-the-sonar-plugin-fail-maveninstallation-nosuchmethoderror/
            // indicates that this kind of inconsistency can happen. So let's be defensive
            throw new IOException("Inconsistent file length: expected " + total + " but only got " + tmp.length());
        }
        return tmp;
    } catch (IOException e) {
        throw new IOException2("Failed to download from " + src, e);
    } finally {
        if (in != null) {
            try {
                in.close();
            } catch (IOException ioe) {
                // swallow exception
            }
        }
        if (out != null) {
            try {
                out.close();
            } catch (IOException ioe) {
                // swallow exception
            }
        }
    }
}