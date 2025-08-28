    public XMLSignatureInput engineResolveURI(ResourceResolverContext context)
        throws ResourceResolverException {
        InputStream inputStream = null;
        try {

            // calculate new URI
            URI uriNew = getNewURI(context.uriToResolve, context.baseUri);
            URL url = uriNew.toURL();
            URLConnection urlConnection;
            urlConnection = openConnection(url);

            // check if Basic authentication is required
            String auth = urlConnection.getHeaderField("WWW-Authenticate");

            if (auth != null && auth.startsWith("Basic")) {
                // do http basic authentication
                String user =
                    engineGetProperty(ResolverDirectHTTP.properties[ResolverDirectHTTP.HttpBasicUser]);
                String pass =
                    engineGetProperty(ResolverDirectHTTP.properties[ResolverDirectHTTP.HttpBasicPass]);

                if ((user != null) && (pass != null)) {
                    urlConnection = openConnection(url);

                    String password = user + ":" + pass;
                    String encodedPassword = Base64.encode(password.getBytes("ISO-8859-1"));

                    // set authentication property in the http header
                    urlConnection.setRequestProperty("Authorization",
                                                     "Basic " + encodedPassword);
                }
            }

            String mimeType = urlConnection.getHeaderField("Content-Type");
            inputStream = urlConnection.getInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte buf[] = new byte[4096];
            int read = 0;
            int summarized = 0;

            while ((read = inputStream.read(buf)) >= 0) {
                baos.write(buf, 0, read);
                summarized += read;
            }

            if (log.isLoggable(java.util.logging.Level.FINE)) {
                log.log(java.util.logging.Level.FINE, "Fetched " + summarized + " bytes from URI " + uriNew.toString());
            }

            XMLSignatureInput result = new XMLSignatureInput(baos.toByteArray());

            result.setSourceURI(uriNew.toString());
            result.setMIMEType(mimeType);

            return result;
        } catch (URISyntaxException ex) {
            throw new ResourceResolverException("generic.EmptyMessage", ex, context.attr, context.baseUri);
        } catch (MalformedURLException ex) {
            throw new ResourceResolverException("generic.EmptyMessage", ex, context.attr, context.baseUri);
        } catch (IOException ex) {
            throw new ResourceResolverException("generic.EmptyMessage", ex, context.attr, context.baseUri);
        } catch (IllegalArgumentException e) {
            throw new ResourceResolverException("generic.EmptyMessage", e, context.attr, context.baseUri);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    if (log.isLoggable(java.util.logging.Level.FINE)) {
                        log.log(java.util.logging.Level.FINE, e.getMessage(), e);
                    }
                }
            }
        }
    }