private InputStream getInputStream(final FlowFile flowFile, final ContentClaim claim, final long offset) throws ContentNotFoundException {
        // If there's no content, don't bother going to the Content Repository because it is generally expensive and we know
        // that there is no actual content.
        if (flowFile.getSize() == 0L) {
            return new ByteArrayInputStream(new byte[0]);
        }
        try {
            // If the recursion set is empty, we can use the same input stream that we already have open. However, if
            // the recursion set is NOT empty, we can't do this because we may be reading the input of FlowFile 1 while in the
            // callback for reading FlowFile 1 and if we used the same stream we'd be destroying the ability to read from FlowFile 1.
            if (recursionSet.isEmpty()) {
                if (currentReadClaim == claim) {
                    if (currentReadClaimStream != null && currentReadClaimStream.getStreamLocation() <= offset) {
                        final long bytesToSkip = offset - currentReadClaimStream.getStreamLocation();
                        if (bytesToSkip > 0) {
                            StreamUtils.skip(currentReadClaimStream, bytesToSkip);
                        }
                        return new DisableOnCloseInputStream(currentReadClaimStream);
                    }
                }
                final InputStream rawInStream = context.getContentRepository().read(claim);
                if (currentReadClaimStream != null) {
                    currentReadClaimStream.close();
                }
                currentReadClaim = claim;
                currentReadClaimStream = new ByteCountingInputStream(rawInStream, new LongHolder(0L));
                StreamUtils.skip(currentReadClaimStream, offset);
                // Use a non-closeable stream because we want to keep it open after the callback has finished so that we can
                // reuse the same InputStream for the next FlowFile
                return new DisableOnCloseInputStream(currentReadClaimStream);
            } else {
                final InputStream rawInStream = context.getContentRepository().read(claim);
                try {
                    StreamUtils.skip(rawInStream, offset);
                } catch(IOException ioe) {
                    IOUtils.closeQuietly(rawInStream);
                    throw ioe;
                }
                return rawInStream;
            }
        } catch (final ContentNotFoundException cnfe) {
            throw cnfe;
        } catch (final EOFException eof) {
            throw new ContentNotFoundException(claim, eof);
        } catch (final IOException ioe) {
            throw new FlowFileAccessException("Failed to read content of " + flowFile, ioe);
        }
    }