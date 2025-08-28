    private static String exec(final String... cmd) throws IOException, InterruptedException {
        assert cmd != null;

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Log.trace("Running: ", cmd);

        Process p = Runtime.getRuntime().exec(cmd);
        int c;

        InputStream in = p.getInputStream();

        while ((c = in.read()) != -1) {
            out.write(c);
        }

        in = p.getErrorStream();

        while ((c = in.read()) != -1) {
            out.write(c);
        }

        p.waitFor();

        String result = new String(out.toByteArray());

        Log.trace("Result: ", result);

        return result;
    }
