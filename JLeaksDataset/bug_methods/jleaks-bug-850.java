    public void apply(Request request, Response response) {
        try {
            if (name != null) {
                setContentTypeIfNotSet(response, MimeTypes.getContentType(name));
            }
            if (contentType != null) {
                response.contentType = contentType;
            }
            String dispositionType;
            if(inline) {
                dispositionType = INLINE_DISPOSITION_TYPE;
            } else {
                dispositionType = ATTACHMENT_DISPOSITION_TYPE;
            }
            if (!response.headers.containsKey("Content-Disposition")) {
                if(name == null) {
                    response.setHeader("Content-Disposition", dispositionType);
                } else {
                    if(canAsciiEncode(name)) {
                        String contentDisposition = "%s; filename=\"%s\"";
                        response.setHeader("Content-Disposition", String.format(contentDisposition, dispositionType, name));
                    } else {
                        final String encoding = getEncoding();
                        String contentDisposition = "%1$s; filename*="+encoding+"''%2$s; filename=\"%2$s\"";
                        response.setHeader("Content-Disposition", String.format(contentDisposition, dispositionType, encoder.encode(name, encoding)));
                    }
                }
            }
            if (file != null) {
                if (!file.exists()) {
                    throw new UnexpectedException("Your file does not exists (" + file + ")");
                }
                if (!file.canRead()) {
                    throw new UnexpectedException("Can't read your file (" + file + ")");
                }
                if (!file.isFile()) {
                    throw new UnexpectedException("Your file is not a real file (" + file + ")");
                }
                response.direct = file;
            } else {
                if (response.getHeader("Content-Length") != null) {
                    response.direct = is;
                } else {
                    if (length != 0) {
                        response.setHeader("Content-Length", length + "");
                        response.direct = is;
                    } else {
                        byte[] buffer = new byte[8092];
                        int count = 0;
                        while ((count = is.read(buffer)) > 0) {
                            response.out.write(buffer, 0, count);
                        }
                        is.close();
                    }
                }
            }
        } catch (Exception e) {
            throw new UnexpectedException(e);
        }
    }
