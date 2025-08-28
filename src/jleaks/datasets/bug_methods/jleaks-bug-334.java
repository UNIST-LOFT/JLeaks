    private String getBodyAsMessage(ClientResponseContext responseContext) throws IOException {
        if (responseContext.hasEntity()) {
            int contentLength = responseContext.getLength();
            if (contentLength != -1) {
                byte[] buffer = new byte[contentLength];
                try {
                    InputStream entityStream = responseContext.getEntityStream();
                    IOUtils.readFully(entityStream, buffer);
                    entityStream.close();
                } catch (EOFException e) {
                    return null;
                }
                Charset charset = null;
                MediaType mediaType = responseContext.getMediaType();
                if (mediaType != null) {
                    String charsetName = mediaType.getParameters().get("charset");
                    if (charsetName != null) {
                        try {
                            charset = Charset.forName(charsetName);
                        } catch (Exception e) {
                            // Do noting...
                        }
                    }
                }
                if (charset == null) {
                    charset = Charset.defaultCharset();
                }
                return new String(buffer, charset);
            }
        }
        return null;
    }
