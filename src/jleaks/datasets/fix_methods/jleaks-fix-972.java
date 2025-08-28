    protected String sendEntityData( HttpEntityEnclosingRequestBase entity) throws IOException {
        boolean hasEntityBody = false;

        final HTTPFileArg[] files = getHTTPFiles();
        // Allow the mimetype of the file to control the content type
        // This is not obvious in GUI if you are not uploading any files,
        // but just sending the content of nameless parameters
        final HTTPFileArg file = files.length > 0? files[0] : null;
        String contentTypeValue = null;
        if(file != null && file.getMimeType() != null && file.getMimeType().length() > 0) {
            contentTypeValue = file.getMimeType();
            entity.setHeader(HEADER_CONTENT_TYPE, contentTypeValue); // we provide the MIME type here
        }

        // Check for local contentEncoding (charset) override; fall back to default for content body
        // we do this here rather so we can use the same charset to retrieve the data
        final String charset = getContentEncoding(HTTP.DEF_CONTENT_CHARSET.name());

        // Only create this if we are overriding whatever default there may be
        // If there are no arguments, we can send a file as the body of the request

        if(!hasArguments() && getSendFileAsPostBody()) {
            hasEntityBody = true;

            // If getSendFileAsPostBody returned true, it's sure that file is not null
            File reservedFile = FileServer.getFileServer().getResolvedFile(files[0].getPath());
            FileEntity fileRequestEntity = new FileEntity(reservedFile); // no need for content-type here
            entity.setEntity(fileRequestEntity);
        }
        // If none of the arguments have a name specified, we
        // just send all the values as the entity body
        else if(getSendParameterValuesAsPostBody()) {
            hasEntityBody = true;

            // Just append all the parameter values, and use that as the entity body
            Arguments arguments = getArguments();
            StringBuilder entityBodyContent = new StringBuilder(arguments.getArgumentCount()*15);
            for (JMeterProperty jMeterProperty : arguments) {
                HTTPArgument arg = (HTTPArgument) jMeterProperty.getObjectValue();
                // Note: if "Encoded?" is not selected, arg.getEncodedValue is equivalent to arg.getValue
                if (charset != null) {
                    entityBodyContent.append(arg.getEncodedValue(charset));
                } else {
                    entityBodyContent.append(arg.getEncodedValue());
                }
            }
            StringEntity requestEntity = new StringEntity(entityBodyContent.toString(), charset);
            entity.setEntity(requestEntity);
        }
        // Check if we have any content to send for body
        if(hasEntityBody) {
            // If the request entity is repeatable, we can send it first to
            // our own stream, so we can return it
            final HttpEntity entityEntry = entity.getEntity();
            // Buffer to hold the entity body
            StringBuilder entityBody = null;
            if(entityEntry.isRepeatable()) {
                entityBody = new StringBuilder(1000);
                // FIXME Charset
                try (InputStream in = entityEntry.getContent();
                        InputStream bounded = new BoundedInputStream(in, MAX_BODY_RETAIN_SIZE)) {
                    entityBody.append(IOUtils.toString(bounded));
                }
                if (entityEntry.getContentLength() > MAX_BODY_RETAIN_SIZE) {
                    entityBody.append("<actual file content shortened>");
                }
            }
            else { 
                entityBody = new StringBuilder(65);
                // this probably cannot happen
                entityBody.append("<RequestEntity was not repeatable, cannot view what was sent>");
            }
            return entityBody.toString();
        }
        return ""; // may be the empty string
    }
