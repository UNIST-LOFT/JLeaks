private String getBodyAsMessage(ClientResponseContext responseContext) throws IOException 
{
    if (responseContext.hasEntity()) {
        try (InputStream entityStream = responseContext.getEntityStream()) {
            Charset charset = null;
            MediaType mediaType = responseContext.getMediaType();
            if (mediaType != null) {
                String charsetName = mediaType.getParameters().get("charset");
                if (charsetName != null) {
                    try {
                        charset = Charset.forName(charsetName);
                    } catch (Exception ignored) {
                    }
                }
            }
            if (charset == null) {
                charset = Charset.defaultCharset();
            }
            return IOUtils.toString(entityStream, charset);
        } catch (Exception ignored) {
        }
    }
    return null;
}