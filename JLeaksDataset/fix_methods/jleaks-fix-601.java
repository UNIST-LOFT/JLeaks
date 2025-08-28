static void writeSymlink(File cache, String target) throws IOException, InterruptedException 
{
    StringWriter w = new StringWriter();
    StreamTaskListener listener = new StreamTaskListener(w);
    File tmp = new File(cache.getPath() + ".tmp");
    try {
        Util.createSymlink(tmp.getParentFile(), target, tmp.getName(), listener);
        // Avoid calling resolveSymlink on a nonexistent file as it will probably throw an IOException:
        if (!exists(tmp) || Util.resolveSymlink(tmp) == null) {
            // symlink not supported. use a regular file
            AtomicFileWriter cw = new AtomicFileWriter(cache);
            try {
                cw.write(target);
                cw.commit();
            } finally {
                try {
                    cw.abort();
                } catch (IOException ioe) {
                    // swallow exception
                }
                try {
                    cw.close();
                } catch (IOException ioe) {
                    // swallow exception
                }
            }
        } else {
            cache.delete();
            tmp.renameTo(cache);
        }
    } finally {
        tmp.delete();
    }
}