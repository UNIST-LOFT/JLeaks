public long exportTo(ContentClaim claim, OutputStream destination) throws IOException {
        final InputStream in = read(claim);
        try {
            return StreamUtils.copy(in, destination);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }