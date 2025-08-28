  static InputStream injectedStream(HttpURLConnection conn) throws IOException {
    if (conn.getErrorStream() != null) {
      return conn.getInputStream();
    }
    byte[] connBytes = toBytes(conn.getInputStream());
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
        synchronized (injectorLock) {
          for (Injector injector : injectors) {
            byte[] newContent = injector.inject(conn, content);
            if (newContent != null) {
              content = newContent;
            }
          }
        }
        if (content != null) {
          if ("gzip".equalsIgnoreCase(encoding)) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            GZIPOutputStream gzip = new GZIPOutputStream(out);
            gzip.write(content);
            gzip.close();
            return new ByteArrayInputStream(out.toByteArray());
          }
          if ("deflate".equalsIgnoreCase(encoding)) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DeflaterOutputStream deflate = new DeflaterOutputStream(out);
            deflate.write(content);
            deflate.close();
            return new ByteArrayInputStream(out.toByteArray());
          }
          return new ByteArrayInputStream(content);
        }
      } catch (Throwable t) {
        Logs.exception(t);
      }
    }
    return new ByteArrayInputStream(connBytes);
  }
