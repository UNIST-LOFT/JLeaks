    public String getBody() {
        InputStream body = null;
        try {
            body = connection.getInputStream();
            if (body != null) {
                StringBuilderWriter writer = new StringBuilderWriter();
                try {
                    IOUtils.copy(body, writer);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return writer.getBuilder().toString();
            } else {
                return null;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(body);
        }
    }
