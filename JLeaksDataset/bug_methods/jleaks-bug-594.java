        private void testConnection(URL url) throws IOException {
            try {
                URLConnection connection = (URLConnection) ProxyConfiguration.open(url);

                if(connection instanceof HttpURLConnection) {
                    int responseCode = ((HttpURLConnection)connection).getResponseCode();
                    if(HttpURLConnection.HTTP_OK != responseCode) {
                        throw new HttpRetryException("Invalid response code (" + responseCode + ") from URL: " + url, responseCode);
                    }
                } else {
                    Util.copyStreamAndClose(connection.getInputStream(),new NullOutputStream());
                }
            } catch (SSLHandshakeException e) {
                if (e.getMessage().contains("PKIX path building failed"))
                   // fix up this crappy error message from JDK
                    throw new IOException("Failed to validate the SSL certificate of "+url,e);
            }
        }