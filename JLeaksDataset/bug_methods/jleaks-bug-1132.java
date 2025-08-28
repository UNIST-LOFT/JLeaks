    public long exportTo(ContentClaim claim, OutputStream destination) throws IOException {
        final InputStream in = read(claim);
        return StreamUtils.copy(in, destination);
    }
