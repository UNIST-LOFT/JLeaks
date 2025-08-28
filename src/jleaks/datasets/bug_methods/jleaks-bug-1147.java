    protected boolean checkAndLoadCache() {
        long time = System.currentTimeMillis();
        if (lastChecked > 0) {
            if (expiration < 0 || time - lastChecked < expiration) {
                return false;
            }
        }
        try {
            URLConnection connection = new java.net.URL(url).openConnection();
            if (connection instanceof HttpURLConnection) {
                HttpURLConnection con = (HttpURLConnection) connection;
                if (lastModified > 0) {
                    con.setIfModifiedSince(lastModified);
                }
                con.setRequestProperty(HEADER_ACCEPT_ENCODING, GZIP);
                int rc = con.getResponseCode();
                if (rc == HTTP_NOT_MODIFIED) {
                    lastChecked = time;
                    return false;
                }
                if (rc != HTTP_OK) {
                    throw new IOException("Unexpected http response loading " + url + " : " + rc + " " + con.getResponseMessage());
                }
            }
            long lm = connection.getLastModified();
            if (lm > 0 && lm <= lastModified) {
                lastChecked = time;
                return false;
            }
            try (
                    BufferedInputStream bis = new BufferedInputStream(connection.getInputStream())
            ) {
                // Auto-detect gzipped streams
                InputStream is = bis;
                bis.mark(512);
                int b0 = bis.read();
                int b1 = bis.read();
                bis.reset();
                if (b0 == 0x1f && b1 == 0x8b) {
                    is = new GZIPInputStream(bis);
                }
                boolean r = doRead(is);
                lastModified = lm;
                lastChecked = time;
                return r;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
