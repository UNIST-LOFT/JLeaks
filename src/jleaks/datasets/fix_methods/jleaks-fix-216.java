    public String getBody() {
        try (InputStream body = connection.getInputStream();
             StringBuilderWriter writer = new StringBuilderWriter()) {
            if (body != null) {
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
        }
    }
