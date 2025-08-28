    private boolean consumeWithFileResourceHandlers(HttpServletRequest httpRequest,
                                                    HttpServletResponse httpResponse) throws IOException {
        if (staticResourceHandlers != null) {

            for (AbstractResourceHandler staticResourceHandler : staticResourceHandlers) {

                AbstractFileResolvingResource resource = staticResourceHandler.getResource(httpRequest);

                if (resource != null && resource.isReadable()) {

                    if (MimeType.shouldGuess()) {
                        httpResponse.setHeader(MimeType.CONTENT_TYPE, MimeType.fromResource(resource));
                    }
                    customHeaders.forEach(httpResponse::setHeader); //add all user-defined headers to response

                    try (InputStream inputStream = resource.getInputStream();
                         OutputStream wrappedOutputStream = GzipUtils.checkAndWrap(httpRequest, httpResponse, false)) {
                        IOUtils.copy(inputStream, wrappedOutputStream);
                    }

                    return true;
                }
            }

        }
        return false;
    }
