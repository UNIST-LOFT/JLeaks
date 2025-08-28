static InputStream injectedStream(HttpURLConnection conn) throws IOException 
{
    if (conn.getErrorStream() != null) {
        return conn.getInputStream();
    }
    byte[] connBytes = toBytes(conn.getInputStream());
    Util.close(conn.getInputStream());
    if (connBytes.length > 0) {
        try {
            String encoding = conn.getContentEncoding();
            InputStream in = new ByteArrayInputStream(connBytes);
            if ("gzip".equalsIgnoreCase(encoding)) {
                in = new GZIPInputStream(in);
            } else if ("deflate".equalsIgnoreCase(encoding)) {
                in = new InflaterInputStream(in);
            }
            byte[] content = toBytes(in);
            Util.close(in);
            synchronized (injectorLock) {
                for (Injector injector : injectors) {
                    byte[] newContent = injector.inject(conn, content);
                    if (newContent != null) {
                        content = newContent;
                    }
                }
            }
            if (content != null) {
                ByteArrayOutputStream out = null;
                try {
                    if ("gzip".equalsIgnoreCase(encoding)) {
                        out = new ByteArrayOutputStream();
                        GZIPOutputStream gzip = new GZIPOutputStream(out);
                        gzip.write(content);
                        Util.close(gzip);
                        return new ByteArrayInputStream(out.toByteArray());
                    }
                    if ("deflate".equalsIgnoreCase(encoding)) {
                        out = new ByteArrayOutputStream();
                        DeflaterOutputStream deflate = new DeflaterOutputStream(out);
                        deflate.write(content);
                        Util.close(deflate);
                        return new ByteArrayInputStream(out.toByteArray());
                    }
                } finally {
                    Util.close(out);
                }
                return new ByteArrayInputStream(content);
            }
        } catch (Throwable t) {
            Logs.exception(t);
        }
    }
    return new ByteArrayInputStream(connBytes);
}