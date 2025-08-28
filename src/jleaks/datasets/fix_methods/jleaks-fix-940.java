public boolean get(final String remotePath, final InputStreamCallback callback) 
{
    Assert.notNull(remotePath, "'remotePath' cannot be null");
    return execute(session -> {
        InputStream inputStream = null;
        try {
            inputStream = session.readRaw(remotePath);
            callback.doWithInputStream(inputStream);
            return session.finalizeRaw();
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    });
}