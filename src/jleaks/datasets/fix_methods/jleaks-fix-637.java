private static String exec(final String... cmd) throws IOException, InterruptedException 
{
    assert cmd != null;
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Log.trace("Running: ", cmd);
    Process p = Runtime.getRuntime().exec(cmd);
    InputStream in = null;
    InputStream err = null;
    try {
        int c;
        in = p.getInputStream();
        while ((c = in.read()) != -1) {
            out.write(c);
        }
        err = p.getErrorStream();
        while ((c = err.read()) != -1) {
            out.write(c);
        }
        p.waitFor();
    } finally {
        try {
            in.close();
        } catch (Exception e) {
            // Ignore
        }
        try {
            err.close();
        } catch (Exception e) {
            // Ignore
        }
    }
    String result = new String(out.toByteArray());
    Log.trace("Result: ", result);
    return result;
}