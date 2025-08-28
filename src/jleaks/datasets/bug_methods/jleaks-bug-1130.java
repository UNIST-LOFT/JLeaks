    public InputStream read(final ContentClaim claim) throws IOException {
        if (claim == null) {
            return new ByteArrayInputStream(new byte[0]);
        }
        final Path path = getPath(claim, true);
        final FileInputStream fis = new FileInputStream(path.toFile());
        if (claim.getOffset() > 0L) {
            StreamUtils.skip(fis, claim.getOffset());
        }

        // see javadocs for claim.getLength() as to why we do this.
        if (claim.getLength() >= 0) {
            return new LimitedInputStream(fis, claim.getLength());
        } else {
            return fis;
        }
    }
